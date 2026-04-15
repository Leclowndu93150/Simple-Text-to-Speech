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

    publishing {
        changelog = "Initial release."
        type = STABLE

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1513991"
        }

        dependencies {
            requires("simple-voice-chat")
            requires("yacl")
        }
    }

    sharedCommon {
        dependencies {
            compileOnly("org.pitest.voices:chorus:0.0.9")
            compileOnly("org.pitest.voices:core:0.0.9")
            compileOnly("org.pitest.voices:g2p:0.0.9")
            compileOnly("org.pitest.voices:piper-runtime:0.0.9")
            compileOnly("org.pitest.voices:model-downloader:0.0.9")
            compileOnly("org.pitest.voices:en_us:0.0.9")
            compileOnly("com.microsoft.onnxruntime:onnxruntime:1.20.0")
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
                jarJar("org.pitest.voices:chorus:0.0.9")
                jarJar("org.pitest.voices:core:0.0.9")
                jarJar("org.pitest.voices:g2p:0.0.9")
                jarJar("org.pitest.voices:piper-runtime:0.0.9")
                jarJar("org.pitest.voices:model-downloader:0.0.9")
                jarJar("org.pitest.voices:en_us:0.0.9")
                jarJar("com.microsoft.onnxruntime:onnxruntime:1.20.0")
                jarJar("org.apache.opennlp:opennlp-tools:2.5.5")
                jarJar("org.apache.commons:commons-compress:1.28.0")
                jarJar("commons-io:commons-io:2.20.0")
                jarJar("org.apache.commons:commons-lang3:3.19.0")
            }
        }
        forge {
            loaderVersion = "47.4.20"
            loaderVersionRange = "[47,)"
            dependencies {
                modImplementation("curse.maven:simple-voice-chat-416089:7905002")
                modImplementation("dev.isxander:yet-another-config-lib:3.6.6+1.20.1-forge")
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
    }
}
