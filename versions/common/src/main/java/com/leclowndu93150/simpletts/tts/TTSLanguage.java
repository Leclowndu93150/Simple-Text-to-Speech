package com.leclowndu93150.simpletts.tts;

public enum TTSLanguage {
    EN_GB("English (British)", "en_GB"),
    EN_US("English (American)", "en_US"),
    DE_DE("German", "de_DE"),
    ES_ES("Spanish (Spain)", "es_ES"),
    ES_MX("Spanish (Mexico)", "es_MX"),
    FR_FR("French", "fr_FR"),
    IT_IT("Italian", "it_IT"),
    PT_BR("Portuguese (Brazil)", "pt_BR"),
    PT_PT("Portuguese (Portugal)", "pt_PT"),
    NL_NL("Dutch", "nl_NL"),
    NL_BE("Dutch (Belgium)", "nl_BE"),
    PL_PL("Polish", "pl_PL"),
    SV_SE("Swedish", "sv_SE"),
    DA_DK("Danish", "da_DK"),
    CA_ES("Catalan", "ca_ES");

    private final String displayName;
    private final String locale;

    TTSLanguage(String displayName, String locale) {
        this.displayName = displayName;
        this.locale = locale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLocale() {
        return locale;
    }

    public static TTSLanguage fromLocale(String locale) {
        for (TTSLanguage lang : values()) {
            if (lang.locale.equals(locale)) {
                return lang;
            }
        }
        return EN_GB;
    }
}
