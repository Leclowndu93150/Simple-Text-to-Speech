package com.leclowndu93150.simpletts.tts;

import org.pitest.voices.Language;
import org.pitest.voices.Model;
import org.pitest.voices.download.FileModel;
import org.pitest.voices.piper.PiperHandler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TTSVoice {

    private static final Map<TTSLanguage, List<TTSVoice>> VOICES_BY_LANGUAGE = new LinkedHashMap<>();

    private final String displayName;
    private final String id;
    private final TTSLanguage language;
    private Model cachedModel;
    private final ModelFactory modelFactory;

    @FunctionalInterface
    interface ModelFactory {
        Model create();
    }

    private TTSVoice(String displayName, String id, TTSLanguage language, ModelFactory factory) {
        this.displayName = displayName;
        this.id = id;
        this.language = language;
        this.modelFactory = factory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public TTSLanguage getLanguage() {
        return language;
    }

    public boolean isBundled() {
        return false;
    }

    public boolean requiresDownload() {
        return true;
    }

    public Model getModel() {
        if (cachedModel == null) {
            cachedModel = modelFactory.create();
        }
        return cachedModel;
    }

    public static List<TTSVoice> getVoicesForLanguage(TTSLanguage language) {
        return VOICES_BY_LANGUAGE.getOrDefault(language, Collections.emptyList());
    }

    public static TTSVoice fromId(String id) {
        for (List<TTSVoice> voices : VOICES_BY_LANGUAGE.values()) {
            for (TTSVoice voice : voices) {
                if (voice.id.equals(id)) {
                    return voice;
                }
            }
        }
        return getVoicesForLanguage(TTSLanguage.EN_GB).get(0);
    }

    public static List<TTSVoice> getAllVoices() {
        List<TTSVoice> all = new ArrayList<>();
        VOICES_BY_LANGUAGE.values().forEach(all::addAll);
        return all;
    }

    private static void addDownloadable(TTSLanguage lang, String display, String modelName, Language piperLang, float gain) {
        TTSVoice voice = new TTSVoice(display, modelName, lang, () -> sherpaModel(modelName, piperLang, gain));
        VOICES_BY_LANGUAGE.computeIfAbsent(lang, k -> new ArrayList<>()).add(voice);
    }

    private static Model sherpaModel(String name, Language language, float gain) {
        try {
            URL url = new URL("https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-" + name + ".tar.bz2");
            return new FileModel(PiperHandler.piper(), name, "vits-piper-" + name, language, -1, new SherpaModelFetcher(url, name), gain);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create model: " + name, e);
        }
    }

    static {
        addDownloadable(TTSLanguage.EN_GB, "Alba (Medium)", "en_GB-alba-medium", Language.en_GB, 2.0f);
        addDownloadable(TTSLanguage.EN_GB, "Cori (High)", "en_GB-cori-high", Language.en_GB, 2.0f);
        addDownloadable(TTSLanguage.EN_US, "Bryce (Medium)", "en_US-bryce-medium", Language.en_US, 1.0f);

        addDownloadable(TTSLanguage.EN_GB, "Alan (Medium)", "en_GB-alan-medium", Language.en_GB, 0.8f);
        addDownloadable(TTSLanguage.EN_GB, "Jenny Dioco (Medium)", "en_GB-jenny_dioco-medium", Language.en_GB, 1.8f);
        addDownloadable(TTSLanguage.EN_GB, "Northern English Male (Medium)", "en_GB-northern_english_male-medium", Language.en_GB, 1.1f);
        addDownloadable(TTSLanguage.EN_GB, "Semaine (Medium)", "en_GB-semaine-medium", Language.en_GB, 1.0f);
        addDownloadable(TTSLanguage.EN_GB, "Southern English Female (Low)", "en_GB-southern_english_female-low", Language.en_GB, 1.0f);

        addDownloadable(TTSLanguage.EN_US, "Amy (Medium)", "en_US-amy-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "HFC Female (Medium)", "en_US-hfc_female-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "HFC Male (Medium)", "en_US-hfc_male-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "Joe (Medium)", "en_US-joe-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "John (Medium)", "en_US-john-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "Kristin (Medium)", "en_US-kristin-medium", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "Lessac (High)", "en_US-lessac-high", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "Ryan (High)", "en_US-ryan-high", Language.en_US, 1.0f);
        addDownloadable(TTSLanguage.EN_US, "Kathleen (Low)", "en_US-kathleen-low", Language.en_US, 1.0f);

        addDownloadable(TTSLanguage.DE_DE, "Thorsten (High)", "de_DE-thorsten-high", Language.de_DE, 1.0f);
        addDownloadable(TTSLanguage.DE_DE, "Thorsten (Medium)", "de_DE-thorsten-medium", Language.de_DE, 1.0f);
        addDownloadable(TTSLanguage.DE_DE, "Thorsten Emotional (Medium)", "de_DE-thorsten_emotional-medium", Language.de_DE, 1.0f);
        addDownloadable(TTSLanguage.DE_DE, "Kerstin (Low)", "de_DE-kerstin-low", Language.de_DE, 1.0f);
        addDownloadable(TTSLanguage.DE_DE, "Ramona (Low)", "de_DE-ramona-low", Language.de_DE, 1.0f);
        addDownloadable(TTSLanguage.DE_DE, "Eva K (Extra Low)", "de_DE-eva_k-x_low", Language.de_DE, 1.0f);

        addDownloadable(TTSLanguage.ES_ES, "Carlfm (Medium)", "es_ES-carlfm-medium", Language.es_ES, 1.0f);
        addDownloadable(TTSLanguage.ES_ES, "Carlfm (Low)", "es_ES-carlfm-low", Language.es_ES, 1.0f);

        addDownloadable(TTSLanguage.ES_MX, "Jaime (Medium)", "es_MX-jaime-medium", Language.es_ES, 1.0f);

        addDownloadable(TTSLanguage.FR_FR, "Siwis (Medium)", "fr_FR-siwis-medium", Language.fr_FR, 1.0f);
        addDownloadable(TTSLanguage.FR_FR, "Siwis (Low)", "fr_FR-siwis-low", Language.fr_FR, 1.0f);
        addDownloadable(TTSLanguage.FR_FR, "Gilles (Medium)", "fr_FR-gilles-medium", Language.fr_FR, 1.0f);
        addDownloadable(TTSLanguage.FR_FR, "Gilles (Low)", "fr_FR-gilles-low", Language.fr_FR, 1.0f);

        addDownloadable(TTSLanguage.IT_IT, "Riccardo (Low)", "it_IT-riccardo-low", Language.it_IT, 1.0f);

        addDownloadable(TTSLanguage.PT_BR, "Faber (Medium)", "pt_BR-faber-medium", Language.pt_BR, 1.0f);

        addDownloadable(TTSLanguage.PT_PT, "Tugalhais (Medium)", "pt_PT-tugalhais-medium", Language.pt_PT, 1.0f);

        addDownloadable(TTSLanguage.NL_NL, "Pim (Medium)", "nl_NL-pim-medium", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_NL, "Ronnie (Medium)", "nl_NL-ronnie-medium", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_NL, "Dii (High)", "nl_NL-dii-high", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_NL, "Miro (High)", "nl_NL-miro-high", Language.nl_NL, 1.0f);

        addDownloadable(TTSLanguage.NL_BE, "Nathalie (Medium)", "nl_BE-nathalie-medium", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_BE, "Nathalie (Extra Low)", "nl_BE-nathalie-x_low", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_BE, "RDH (Medium)", "nl_BE-rdh-medium", Language.nl_NL, 1.0f);
        addDownloadable(TTSLanguage.NL_BE, "RDH (Extra Low)", "nl_BE-rdh-x_low", Language.nl_NL, 1.0f);

        addDownloadable(TTSLanguage.PL_PL, "Darkman (Medium)", "pl_PL-darkman-medium", Language.pl_PL, 1.0f);

        addDownloadable(TTSLanguage.SV_SE, "Frigg (Medium)", "sv_SE-frigg-medium", Language.sv_SE, 1.0f);

        addDownloadable(TTSLanguage.DA_DK, "Talesyntese (Medium)", "da_DK-talesyntese-medium", Language.da_DK, 1.0f);

        addDownloadable(TTSLanguage.CA_ES, "UPC Ona (Medium)", "ca_ES-upc_ona-medium", Language.ca_ES, 1.0f);
    }
}
