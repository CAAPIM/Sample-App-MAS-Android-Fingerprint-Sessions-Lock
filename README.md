# Session Unlock Sample App

**Required:**
* Latest version of Android Studio
* Device with Passcode and/or Fingerprint locks enabled

## Get Started
1. In Android Studio, open the project 'MASSessionUnlockSample'.
2. Go to your CA Mobile API Gateway policy manager (or Mobile Developer Console if you have one), create an app, and export the msso_config (https://you_server_name:8443/oauth/manager) For more info, see [Android Guide](https://www.ca.com/us/developers/mas/docs.html?id=1).
3. Copy the contents of the exported msso_config into src/main/assets/msso_config.json.
4. Build and Deploy the app to a device.
