package com.example.altcheck;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

@Mod(modid = AltCheckMod.MODID, version = AltCheckMod.VERSION)
public class AltCheckMod {
    public static final String MODID = "ACM";
    public static final String VERSION = "1.0";
    private final Minecraft mc = Minecraft.getMinecraft();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    private static final Path CONFIG_PATH = Paths.get(Minecraft.getMinecraft().mcDataDir.getPath(), "altcheck.properties");
    private static String POLSU_API_KEY = "";

    static {
        loadApiKey();
    }

    private static void loadApiKey() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                Properties prop = new Properties();
                prop.load(input);
                POLSU_API_KEY = prop.getProperty("POLSU_API_KEY", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveApiKey(String apiKey) {
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            Properties prop = new Properties();
            prop.setProperty("POLSU_API_KEY", apiKey);
            prop.store(output, null);
            POLSU_API_KEY = apiKey;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandAltCheck());
    }

    public class CommandAltCheck extends CommandBase {
        @Override
        public String getCommandName() {
            return "altcheck";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 2 && args[0].equalsIgnoreCase("apikey")) {
                saveApiKey(args[1]);
                sendMessage(sender, "§aAPI key set successfully.");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("viewkey")) {
                    if (POLSU_API_KEY.isEmpty()) {
                        sendMessage(sender, "§cNo API Key Set.");
                    } else {
                        sendMessage(sender, "§aCurrent API Key: §e" + POLSU_API_KEY);
                    }
                } else {
                    if (POLSU_API_KEY.isEmpty()) {
                        sendMessage(sender, "§cNo API Key Set. Fetch one from https://polsu.xyz/api/apikey");
                        return;
                    }
                    new Thread(() -> checkAltsAsync(sender, args[0])).start();
                }
            } else {
                sendMessage(sender, "§cUsage: /altcheck <username>, /altcheck apikey <api-key>, or /altcheck viewkey");
            }
        }

        private void checkAltsAsync(ICommandSender sender, String username) {
            try {
                ProfileData profile = fetchMojangProfile(username);
                if (profile == null) {
                    sendMessage(sender, "§cPlayer not found!");
                    return;
                }
                Set<String> alts = fetchPolsuAlts(profile.uuid);
                sendResults(sender, profile.currentUsername, alts);
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(sender, "§cCheck failed: " + e.getMessage());
            }
        }

        private ProfileData fetchMojangProfile(String username) throws IOException {
            Request request = new Request.Builder().url("https://api.mojang.com/users/profiles/minecraft/" + username).build();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) return null;
            JsonObject json = gson.fromJson(response.body().charStream(), JsonObject.class);
            return new ProfileData(json.get("name").getAsString(), json.get("id").getAsString());
        }

        private Set<String> fetchPolsuAlts(String uuid) throws IOException {
            Set<String> alts = new HashSet<>();
            String urlWithParams = "https://api.polsu.xyz/polsu/bedwars/quickbuy/all?uuid=" + uuid;
            Request request = new Request.Builder()
                    .url(urlWithParams)
                    .get()
                    .addHeader("API-Key", POLSU_API_KEY)
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (response.code() == 200) {
                JsonObject json = gson.fromJson(response.body().charStream(), JsonObject.class);
                if (json.has("success") && json.get("success").getAsBoolean()) {
                    alts = parsePolsuData(json);
                }
            }
            return alts;
        }

        private Set<String> parsePolsuData(JsonObject json) {
            Set<String> usernames = new HashSet<>();
            if (json.has("data")) {
                JsonObject data = json.getAsJsonObject("data");
                if (data.has("quickbuy")) {
                    JsonArray quickbuyArray = data.getAsJsonArray("quickbuy");
                    for (JsonElement element : quickbuyArray) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("username")) {
                            usernames.add(obj.get("username").getAsString());
                        }
                    }
                }
            }
            return usernames;
        }

        private void sendResults(ICommandSender sender, String username, Set<String> alts) {
            mc.addScheduledTask(() -> {
                StringBuilder sb = new StringBuilder("§6Alt Check Results:\n");
                if (alts.isEmpty()) {
                    sb.append("§aNo alts found for §e").append(username);
                } else {
                    sb.append("§c").append(alts.size()).append(" alts found for §e").append(username).append("§c:\n");
                    alts.forEach(alt -> sb.append("§7- §f").append(alt).append("\n"));
                }
                sender.addChatMessage(new ChatComponentText(sb.toString()));
            });
        }

        private void sendMessage(ICommandSender sender, String message) {
            mc.addScheduledTask(() -> sender.addChatMessage(new ChatComponentText(message)));
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/altcheck <username>, /altcheck apikey <api-key>, or /altcheck viewkey";
        }
    }

    private static class ProfileData {
        public final String currentUsername;
        public final String uuid;

        public ProfileData(String currentUsername, String uuid) {
            this.currentUsername = currentUsername;
            this.uuid = uuid;
        }
    }
private void sendMessage(ICommandSender sender, String message) {
    mc.addScheduledTask(() -> {
        if (message.contains("https://")) {
            String[] parts = message.split(" ");
            IChatComponent chatComponent = new ChatComponentText("");
            for (String part : parts) {
                if (part.startsWith("https://") || part.startsWith("http://")) {
                    ChatComponentText link = new ChatComponentText(part);
                    link.getChatStyle().setUnderlined(true)
                            .setColor(EnumChatFormatting.AQUA)
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, part))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to open")));
                    chatComponent.appendSibling(link);
                } else {
                    chatComponent.appendSibling(new ChatComponentText(part + " "));
                }
            }
            sender.addChatMessage(chatComponent);
        } else {
            sender.addChatMessage(new ChatComponentText(message));
        }
    });
}}
