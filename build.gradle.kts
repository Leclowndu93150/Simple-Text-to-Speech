plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "1.0.0"

subprojects {
    configurations.configureEach {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
}

prism {
    metadata {
        modId = "simpletts"
        name = "Simple Text to Speech"
        description = "Text-to-Speech through Simple Voice Chat using neural voices"
        license = "MIT"
        author("Leclowndu93150")
    }

    curseMaven()

    maven("henkelmax", "https://maven.maxhenkel.de/repository/public")
    maven("xander", "https://maven.isxander.dev/releases")

    sharedCommon {
        dependencies {
            shadow("org.pitest.voices:chorus:0.0.9")
            shadow("org.pitest.voices:core:0.0.9")
            shadow("org.pitest.voices:g2p:0.0.9")
            shadow("org.pitest.voices:piper-runtime:0.0.9")
            shadow("org.pitest.voices:model-downloader:0.0.9")
            shadow("org.pitest.voices:en_us:0.0.9")
            shadow("com.microsoft.onnxruntime:onnxruntime:1.20.0") {
                excludeRelocation("ai/onnxruntime/**")
                strip("META-INF/services/**")
            }
        }
    }

    version("1.20.1") {
        common {
            dependencies {
                compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.0")
            }
        }
        fabric {
            loaderVersion = "0.19.1"
            fabricApi("0.92.7+1.20.1")
            dependencies {
                modImplementation("curse.maven:simple-voice-chat-416089:7905011")
                modImplementation("dev.isxander:yet-another-config-lib:3.6.6+1.20.1-fabric")
                modImplementation("curse.maven:modmenu-308702:5162837")
            }
        }
        forge {
            loaderVersion = "47.4.20"
            loaderVersionRange = "[47,)"
            dependencies {
                modImplementation("curse.maven:simple-voice-chat-416089:7905002")
                modImplementation("dev.isxander:yet-another-config-lib:3.6.6+1.20.1-forge")
            }
        }
    }
}
