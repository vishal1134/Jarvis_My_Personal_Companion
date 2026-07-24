package com.vishal.jarvis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalCommandParser {
    public List<JarvisResult> parse(String text, String lastSpokenResponse) {
        ArrayList<String> defaultNames = new ArrayList<>();
        defaultNames.add("jarvis");
        return parse(text, lastSpokenResponse, defaultNames, "jarvis");
    }

    public List<JarvisResult> parse(String text, String lastSpokenResponse, List<String> assistantNames, String activeName) {
        List<String> parts = splitMultiCommand(text);
        ArrayList<JarvisResult> results = new ArrayList<>();
        for (String part : parts) {
            JarvisResult result = parseSingle(part, lastSpokenResponse, assistantNames, activeName);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    private JarvisResult parseSingle(String rawText, String lastSpokenResponse, List<String> assistantNames, String activeName) {
        String source = normalize(rawText);
        String calledName = findWakeName(source, assistantNames);
        String text = removeWakeName(source, calledName);
        boolean tanglish = isTanglish(source);

        if (text.isEmpty() || containsAny(text, "wake up", "you up", "boot up", "turn on",
                "you there", "irukiya", "irukiyaa", "irukingala", "on aagu", "status", "ready ah")) {
            if (calledName != null && activeName != null && !calledName.equals(activeName)) {
                return result("wake_assistant", null, "You selected " + activeName + " as my active name, sir. I will still respond.");
            }
            return result("wake_assistant", null, tanglish
                    ? "Naan inga irukken, sir. Unga command ku ready."
                    : "At your command, sir.");
        }

        if (containsAny(text, "turn off", "sleep", "go offline", "thoongu")) {
            return result("sleep_assistant", null, tanglish
                    ? "Sari sir. Neenga koopidum varai silent ah iruppen."
                    : "Going quiet, sir. Call my name when you need me.");
        }

        if (isHelp(text)) {
            return result("list_commands", null,
                    "I can call contacts, open apps, search YouTube, read notifications, open settings, control flashlight, tell time, and handle Bluetooth or Wi-Fi settings, sir.");
        }

        if (isRepeat(text)) {
            return result("repeat_last_response", null,
                    lastSpokenResponse == null || lastSpokenResponse.isEmpty()
                            ? "I do not have anything to repeat yet, sir."
                            : lastSpokenResponse);
        }

        if (isStop(text)) {
            return result("stop_speaking", null, tanglish ? "Niruthuren, sir." : "Stopping, sir.");
        }

        if (isTime(text)) {
            String time = new SimpleDateFormat("h:mm a", Locale.US).format(new Date());
            return result("get_time", null, tanglish ? "Ippo time " + time + ", sir." : "It is " + time + ", sir.");
        }

        if (isDate(text)) {
            String date = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(new Date());
            return result("get_date", null, tanglish ? "Innaiku " + date + ", sir." : "Today is " + date + ", sir.");
        }

        String youtubeQuery = extractYoutubeQuery(text);
        if (youtubeQuery != null) {
            return result("search_youtube", youtubeQuery, tanglish
                    ? "YouTube la " + youtubeQuery + " search pannuren, sir."
                    : "Searching YouTube for " + youtubeQuery + ", sir.");
        }

        String calculation = extractCalculation(text);
        if (calculation != null) {
            return result("calculate_expression", calculation, tanglish
                    ? calculation + " calculate pannuren, sir."
                    : "Calculating " + calculation + ", sir.");
        }

        String webQuery = extractWebSearchQuery(text);
        if (webQuery != null) {
            return result("search_web", webQuery, tanglish
                    ? "Google la " + webQuery + " search pannuren, sir."
                    : "Searching Google for " + webQuery + ", sir.");
        }

        String appSearch = extractAppSearch(text);
        if (appSearch != null) {
            String[] pieces = appSearch.split("::", 2);
            return result("search_in_app", appSearch, tanglish
                    ? pieces[0] + " la " + pieces[1] + " search pannuren, sir."
                    : "Searching " + pieces[0] + " for " + pieces[1] + ", sir.");
        }

        String notificationApp = extractNotificationApp(text);
        if (notificationApp != null) {
            return result("query_notifications", notificationApp, tanglish
                    ? notificationApp + " notifications check pannuren, sir."
                    : "Checking " + notificationApp + " notifications, sir.");
        }

        if (isReadNotifications(text)) {
            return result("read_notifications", null, tanglish
                    ? "Notifications check pannuren, sir."
                    : "Checking notifications, sir.");
        }

        String bluetoothDevice = extractBluetoothDevice(text);
        if (bluetoothDevice != null) {
            return result("connect_bluetooth", bluetoothDevice, tanglish
                    ? bluetoothDevice + " Bluetooth device connect panna settings open pannuren, sir."
                    : "Opening Bluetooth settings to connect " + bluetoothDevice + ", sir.");
        }

        String bluetoothState = extractBluetoothState(text);
        if (bluetoothState != null) {
            return result("set_bluetooth_state", bluetoothState, tanglish
                    ? "Bluetooth " + bluetoothState + " panna settings open pannuren, sir."
                    : "Opening Bluetooth settings to turn it " + bluetoothState + ", sir.");
        }

        String wifiPassword = extractWifiPassword(text);
        String wifiSsid = extractWifiSsid(text);
        if (wifiSsid != null) {
            return new JarvisResult("connect_wifi", wifiSsid,
                    wifiPassword == null
                            ? "Opening " + wifiSsid + " Wi-Fi. I need the password, sir."
                            : "Trying to connect to " + wifiSsid + " Wi-Fi, sir.",
                    wifiPassword);
        }

        String wifiState = extractWifiState(text);
        if (wifiState != null) {
            return result("set_wifi_state", wifiState, tanglish
                    ? "Wi-Fi " + wifiState + " panna settings open pannuren, sir."
                    : "Opening Wi-Fi settings to turn it " + wifiState + ", sir.");
        }

        String setting = extractSetting(text);
        if (setting != null) {
            return result("open_system_settings", setting, setting + " settings open pannuren, sir.");
        }

        String phoneAction = extractPhoneAction(text);
        if (phoneAction != null) {
            return result("phone_action", phoneAction, phoneActionResponse(phoneAction, tanglish));
        }

        String screenAction = extractScreenAction(text);
        if (screenAction != null) {
            return result("screen_action", screenAction, screenActionResponse(screenAction, tanglish));
        }

        String flashlightState = extractFlashlightState(text);
        if (flashlightState != null) {
            return result("set_flashlight", flashlightState, tanglish
                    ? "Flashlight " + flashlightState + " pannuren, sir."
                    : "Turning " + flashlightState + " flashlight, sir.");
        }

        String contact = extractContact(text);
        if (contact != null) {
            return result("call_contact", contact, tanglish
                    ? contact + " ku call pannuren, sir."
                    : "Calling " + contact + ", sir.");
        }

        String app = extractApp(text);
        if (app != null) {
            return result("open_app", app, tanglish ? app + " open pannuren, sir." : "Opening " + app + ", sir.");
        }

        return result("unknown", null, "I did not understand that yet, sir.");
    }

    private JarvisResult result(String intent, String target, String spokenResponse) {
        return new JarvisResult(intent, target, spokenResponse, null);
    }

    private List<String> splitMultiCommand(String text) {
        String normalized = text.trim();
        String lowered = normalized.toLowerCase(Locale.US);
        String[] separators = {" and then ", " then ", " and ", " appuram ", " apram ", " athukapram "};
        for (String separator : separators) {
            if (lowered.contains(separator)) {
                String[] split = normalized.split("(?i)" + separator.trim());
                ArrayList<String> parts = new ArrayList<>();
                for (String part : split) {
                    if (!part.trim().isEmpty()) {
                        parts.add(part.trim());
                    }
                }
                return parts;
            }
        }
        ArrayList<String> single = new ArrayList<>();
        single.add(normalized);
        return single;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US).trim().replaceAll("\\s+", " ");
    }

    private String findWakeName(String text, List<String> assistantNames) {
        for (String name : assistantNames) {
            String normalizedName = normalize(name);
            if (normalizedName.isEmpty()) {
                continue;
            }
            if (text.equals(normalizedName) || text.startsWith(normalizedName + " ") || text.endsWith(" " + normalizedName)) {
                return normalizedName;
            }
        }
        return null;
    }

    private String removeWakeName(String text, String wakeName) {
        if (wakeName == null || wakeName.isEmpty()) {
            return text;
        }
        if (wakeName.equals(text)) {
            return "";
        }
        if (text.startsWith(wakeName + " ")) {
            return text.substring((wakeName + " ").length()).trim();
        }
        if (text.endsWith(" " + wakeName)) {
            return text.substring(0, text.length() - (" " + wakeName).length()).trim();
        }
        return text;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTanglish(String text) {
        return containsAny(text, "pannu", "la", "ku", "enna", "ena", "sollu", "thira", "podu", "niruthu", "yaaru");
    }

    private boolean isHelp(String text) {
        return containsAny(text, "help", "what can you do", "commands", "udhavi", "enna panna mudiyum", "enna panva");
    }

    private boolean isRepeat(String text) {
        return containsAny(text, "repeat", "say that again", "marubadi sollu", "thirumba sollu");
    }

    private boolean isStop(String text) {
        return containsAny(text, "stop", "cancel", "be quiet", "niruthu", "pesatha");
    }

    private boolean isTime(String text) {
        return containsAny(text, "what time", "current time", "time ena", "time enna", "neram", "mani enna", "mani ena")
                || "time".equals(text);
    }

    private boolean isDate(String text) {
        return containsAny(text, "today date", "what is the date", "date sollu", "thethi", "innaiku date")
                || "date".equals(text);
    }

    private String extractYoutubeQuery(String text) {
        String cleaned = text.replace("you tube", "youtube");
        if (cleaned.startsWith("youtube search ")) {
            return cleaned.substring("youtube search ".length()).trim();
        }
        if (cleaned.startsWith("search ") && cleaned.contains(" in youtube")) {
            return cleaned.substring("search ".length(), cleaned.indexOf(" in youtube")).trim();
        }
        if (cleaned.startsWith("youtube la ") && cleaned.contains(" search")) {
            return cleaned.substring("youtube la ".length(), cleaned.indexOf(" search")).trim();
        }
        if (cleaned.startsWith("youtube la ") && cleaned.contains(" thedu")) {
            return cleaned.substring("youtube la ".length(), cleaned.indexOf(" thedu")).trim();
        }
        return null;
    }

    private String extractCalculation(String text) {
        String cleaned = text.replace("calculator la ", "")
                .replace("calculator", "")
                .replace("calculate pannu", "calculate")
                .trim();
        if (cleaned.startsWith("calculate ")) {
            return cleaned.substring("calculate ".length()).trim();
        }
        if (cleaned.endsWith(" calculate")) {
            return cleaned.substring(0, cleaned.indexOf(" calculate")).trim();
        }
        if (cleaned.contains(" plus ") || cleaned.contains(" minus ")
                || cleaned.contains(" into ") || cleaned.contains(" divided by ")
                || cleaned.matches(".*\\d\\s*[+\\-*/x]\\s*\\d.*")) {
            return cleaned;
        }
        return null;
    }

    private String extractWebSearchQuery(String text) {
        if (text.startsWith("google search ")) {
            return text.substring("google search ".length()).trim();
        }
        if (text.startsWith("search ") && containsAny(text, " in google", " on google")) {
            int end = text.contains(" in google") ? text.indexOf(" in google") : text.indexOf(" on google");
            return text.substring("search ".length(), end).trim();
        }
        if (text.startsWith("google la ") && text.contains(" search")) {
            return text.substring("google la ".length(), text.indexOf(" search")).trim();
        }
        if (text.startsWith("google la ") && text.contains(" thedu")) {
            return text.substring("google la ".length(), text.indexOf(" thedu")).trim();
        }
        return null;
    }

    private String extractAppSearch(String text) {
        String[] apps = {"instagram", "gmail", "mail", "chrome", "google", "play store", "maps", "photos", "telegram"};
        for (String app : apps) {
            String prefix = app + " la ";
            if (text.startsWith(prefix) && text.contains(" search")) {
                String query = text.substring(prefix.length(), text.indexOf(" search")).trim();
                return query.isEmpty() ? null : app + "::" + query;
            }
            if (text.startsWith("search ") && text.contains(" in " + app)) {
                String query = text.substring("search ".length(), text.indexOf(" in " + app)).trim();
                return query.isEmpty() ? null : app + "::" + query;
            }
        }
        return null;
    }

    private String extractNotificationApp(String text) {
        String[] apps = {"whatsapp", "instagram", "telegram", "gmail", "messages", "phone", "sms"};
        for (String app : apps) {
            if (text.contains(app) && containsAny(text, "yaaru", "who", "whom", "message", "messaged", "pannirukaa", "panniruka")) {
                return app;
            }
        }
        return null;
    }

    private boolean isReadNotifications(String text) {
        return containsAny(text, "read notifications", "notification read", "notifications read", "notification padi",
                "notifications padi", "notification sollu", "notification enna", "notification ena");
    }

    private String extractBluetoothDevice(String text) {
        if (!text.contains("bluetooth") || !containsAny(text, "connect", "connect pannu", "pair")) {
            return null;
        }
        String cleaned = text.replace("bluetooth", "").replace("device", "").trim();
        cleaned = cleaned.replace("connect to ", "").replace("connect ", "");
        if (cleaned.contains(" connect pannu")) {
            cleaned = cleaned.substring(0, cleaned.indexOf(" connect pannu")).trim();
        }
        if (cleaned.contains(" connect")) {
            cleaned = cleaned.substring(0, cleaned.indexOf(" connect")).trim();
        }
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String extractBluetoothState(String text) {
        if (!text.contains("bluetooth")) {
            return null;
        }
        if (containsAny(text, " on", "on pannu", "enable", "podu")) {
            return "on";
        }
        if (containsAny(text, " off", "off pannu", "disable", "niruthu")) {
            return "off";
        }
        return null;
    }

    private String extractWifiState(String text) {
        if (!text.contains("wifi") && !text.contains("wi-fi")) {
            return null;
        }
        if (containsAny(text, " on", "on pannu", "enable", "podu")) {
            return "on";
        }
        if (containsAny(text, " off", "off pannu", "disable", "niruthu")) {
            return "off";
        }
        return null;
    }

    private String extractWifiSsid(String text) {
        if (!text.contains("wifi") && !text.contains("wi-fi")) {
            return null;
        }
        if (!containsAny(text, "connect", "connect pannu", "join")) {
            return null;
        }
        String ssidPart = text;
        for (String marker : new String[]{" wifi password vandhu ", " password vandhu ", " password is ", " password "}) {
            if (ssidPart.contains(marker)) {
                ssidPart = ssidPart.substring(0, ssidPart.indexOf(marker));
                break;
            }
        }
        ssidPart = ssidPart.replace("wi-fi", "wifi").replace("wifi", "").replace("network", "").trim();
        ssidPart = ssidPart.replace("connect to ", "").replace("connect ", "");
        if (ssidPart.contains(" connect pannu")) {
            ssidPart = ssidPart.substring(0, ssidPart.indexOf(" connect pannu")).trim();
        }
        if (ssidPart.contains(" connect")) {
            ssidPart = ssidPart.substring(0, ssidPart.indexOf(" connect")).trim();
        }
        return ssidPart.isEmpty() ? null : ssidPart;
    }

    private String extractWifiPassword(String text) {
        for (String marker : new String[]{" wifi password vandhu ", " password vandhu ", " password is ", " password "}) {
            if (text.contains(marker)) {
                return normalizeSpokenPassword(text.substring(text.indexOf(marker) + marker.length()));
            }
        }
        return null;
    }

    private String normalizeSpokenPassword(String text) {
        String cleaned = normalize(text);
        boolean capitalK = cleaned.contains("k capital");
        cleaned = cleaned.replace("k capital letter", "").replace("k capital", "");
        cleaned = cleaned.replace(" at ", "@").replace(" hash ", "#").replace(" dollar ", "$")
                .replace(" dot ", ".").replace(" underscore ", "_").replace(" dash ", "-")
                .replace(" hyphen ", "-").replace(" star ", "*");
        String password = cleaned.replace(" ", "");
        if (capitalK) {
            int index = password.indexOf('k');
            if (index >= 0) {
                password = password.substring(0, index) + "K" + password.substring(index + 1);
            }
        }
        return password;
    }

    private String extractSetting(String text) {
        if (text.contains("wifi") && containsAny(text, "settings", "open", "thira")) {
            return "wifi";
        }
        if (text.contains("bluetooth") && containsAny(text, "settings", "open", "thira")) {
            return "bluetooth";
        }
        if (text.contains("notification") && containsAny(text, "settings", "open", "thira")) {
            return "notifications";
        }
        if (text.contains("accessibility") && containsAny(text, "settings", "open", "thira")) {
            return "accessibility";
        }
        if (containsAny(text, "settings", "phone settings")) {
            return "settings";
        }
        return null;
    }

    private String extractPhoneAction(String text) {
        if (containsAny(text, "go back", "back pannu", "pinadi po", "previous screen")) {
            return "back";
        }
        if (containsAny(text, "go home", "home pannu", "home screen")) {
            return "home";
        }
        if (containsAny(text, "recent apps", "recents", "recent open pannu")) {
            return "recents";
        }
        if (containsAny(text, "open notifications", "notification panel", "notifications open pannu")) {
            return "notifications";
        }
        if (containsAny(text, "scroll down", "keela po", "down pannu")) {
            return "scroll_down";
        }
        if (containsAny(text, "scroll up", "mela po", "up pannu")) {
            return "scroll_up";
        }
        return null;
    }

    private String phoneActionResponse(String action, boolean tanglish) {
        if ("back".equals(action)) {
            return tanglish ? "Back poguren, sir." : "Going back, sir.";
        }
        if ("home".equals(action)) {
            return tanglish ? "Home screen ku poguren, sir." : "Going home, sir.";
        }
        if ("recents".equals(action)) {
            return tanglish ? "Recent apps open pannuren, sir." : "Opening recent apps, sir.";
        }
        if ("notifications".equals(action)) {
            return tanglish ? "Notification panel open pannuren, sir." : "Opening notifications, sir.";
        }
        if ("scroll_down".equals(action)) {
            return tanglish ? "Keela scroll pannuren, sir." : "Scrolling down, sir.";
        }
        return tanglish ? "Mela scroll pannuren, sir." : "Scrolling up, sir.";
    }

    private String extractScreenAction(String text) {
        if (text.startsWith("tap ")) {
            return "tap:" + text.substring("tap ".length()).trim();
        }
        if (text.startsWith("click ")) {
            return "tap:" + text.substring("click ".length()).trim();
        }
        if (text.startsWith("press ")) {
            return "tap:" + text.substring("press ".length()).trim();
        }
        if (text.contains(" tap pannu")) {
            return "tap:" + text.substring(0, text.indexOf(" tap pannu")).trim();
        }
        if (text.contains(" click pannu")) {
            return "tap:" + text.substring(0, text.indexOf(" click pannu")).trim();
        }
        if (text.startsWith("type ")) {
            return "type:" + text.substring("type ".length()).trim();
        }
        if (text.startsWith("enter ")) {
            return "type:" + text.substring("enter ".length()).trim();
        }
        if (text.contains(" type pannu")) {
            return "type:" + text.substring(0, text.indexOf(" type pannu")).trim();
        }
        return null;
    }

    private String screenActionResponse(String action, boolean tanglish) {
        if (action.startsWith("tap:")) {
            String target = action.substring("tap:".length());
            return tanglish ? target + " tap pannuren, sir." : "Tapping " + target + ", sir.";
        }
        String target = action.substring("type:".length());
        return tanglish ? target + " type pannuren, sir." : "Typing " + target + ", sir.";
    }

    private String extractFlashlightState(String text) {
        if (!containsAny(text, "torch", "flashlight", "light")) {
            return null;
        }
        if (containsAny(text, "on", "podu")) {
            return "on";
        }
        if (containsAny(text, "off", "niruthu")) {
            return "off";
        }
        return null;
    }

    private String extractContact(String text) {
        if (text.contains("ku call pannu")) {
            return text.substring(0, text.indexOf("ku call pannu")).trim();
        }
        if (text.contains("ku phone pannu")) {
            return text.substring(0, text.indexOf("ku phone pannu")).trim();
        }
        if (text.endsWith("koopidu")) {
            return text.substring(0, text.indexOf("koopidu")).trim();
        }
        if (text.startsWith("call ")) {
            return text.substring("call ".length()).trim();
        }
        return null;
    }

    private String extractApp(String text) {
        if (text.startsWith("open ")) {
            return text.substring("open ".length()).trim();
        }
        for (String marker : new String[]{" open pannu", " thira", " thirakku"}) {
            if (text.contains(marker)) {
                return text.substring(0, text.indexOf(marker)).trim();
            }
        }
        return null;
    }
}
