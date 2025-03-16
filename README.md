# **Alt Checker**  

**Alt Checker** is a Minecraft mod designed to help players identify alternate accounts (alts) by analyzing in-game behavior. Specifically, it checks for usernames that appear in both quickbuy and hotbar lists, offering a simple way to spot potential alts using a single command.  

## 🌟 **Key Features:**  

- **Accurate Alt Detection:**  
  Utilizes the Polsu API to scan for accounts linked to the given username, comparing quickbuy and hotbar setups across multiple accounts to identify exact matches.  

- **Easy-to-Use Command:**  
  Adds a new `/altcheck` command, making it quick and convenient to analyze players directly in-game.  

- **Asynchronous Processing:**  
  Runs the checks in a separate thread, ensuring your game stays smooth without interruptions while the data is fetched and analyzed.  

- **Seamless Polsu API Integration:**  
  Pulls detailed account data from the Polsu API, providing a reliable source for identifying alternate accounts.  

## ⚙️ **How to Use:**  

1. In the chat, type:  
   ```
   /altcheck <username>
   ```  
2. The mod fetches data for the specified player and compares the quickbuy and hotbar setups.  
3. If matching usernames are found, they’ll be displayed in chat, helping you spot potential alt accounts instantly.  

## 🔍 **How It Works:**  

- When you run the `/altcheck` command, the mod queries the Polsu API for the given username.  
- It parses the data, extracting usernames from quickbuy and hotbar lists.  
- Only usernames that appear in **both lists** are flagged, reducing false positives and improving accuracy.  
- The results are then shown directly in the chat.  

## 📣 **Why Use Alt Checker?**  

Alt Checker is a must-have for competitive Minecraft players, moderators, and community members who want to quickly identify potential alternate accounts. Whether you’re trying to avoid cheaters or keep an eye on suspicious behavior, Alt Checker makes the process fast and effortless.  

## 🏆 **Credits:**  

- **Polsu API:** This mod relies on the Polsu API for accurate alt detection. Huge thanks to Polsu for making this possible!  

Alt Checker adds a new layer of transparency to Minecraft’s multiplayer experience, making it easier than ever to check for alternate accounts. Try it out and take control of your lobbies! 🚀  

