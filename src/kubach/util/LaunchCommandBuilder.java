package kubach.util;

import kubach.ConfigManager;

/**
 * Generates launch command
 *
 * @author Cr0s
 */
public class LaunchCommandBuilder {

    public static String getLaunchCommand(String username, String session, String memory) {

        String forgeVersion = ConfigManager.getInstance().getProperties().getProperty("forgeversion");
        String separator = System.getProperty("file.separator");
        String pathSeparator = System.getProperty("path.separator");
        String gameDir = ConfigManager.getInstance().pathToJar;
        String pathToJre = System.getProperty("java.home");

        StringBuilder args = new StringBuilder();
        
        args.append("java").append(" ");
        args.append("-Xmx").append(memory).append("M ");
        args.append("-Dfml.ignoreInvalidMinecraftCertificates=true ");
        args.append("-Dfml.ignorePatchDiscrepancies=true ");
        args.append("-Djava.library.path=\"./versions/ForgeOptiFine 1.7.10/natives\" ");
        args.append("-cp ");
        args.append(".").append(pathSeparator);
        
        args.append("\"./versions/ForgeOptiFine 1.7.10/ForgeOptiFine 1.7.10.jar\"").append(pathSeparator)
                .append("./libraries/optifine/OptiFine/1.7.10_HD_U_C1/OptiFine-1.7.10_HD_U_C1.jar").append(pathSeparator)
                .append("./libraries/tlauncher/forge/config/FixSplashScreen/1.0/FixSplashScreen-1.0.jar").append(pathSeparator)
                .append("./libraries/net/minecraftforge/forge/1.7.10-10.13.4.1448/forge-1.7.10-10.13.4.1448.jar").append(pathSeparator)
                .append("./libraries/net/minecraft/launchwrapper/1.11/launchwrapper-1.11.jar").append(pathSeparator)
                .append("./libraries/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar").append(pathSeparator)
                .append("./libraries/com/typesafe/akka/akka-actor_2.11/2.3.3/akka-actor_2.11-2.3.3.jar").append(pathSeparator)
                .append("./libraries/com/typesafe/config/1.2.1/config-1.2.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-actors-migration_2.11/1.1.0/scala-actors-migration_2.11-1.1.0.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-compiler/2.11.1/scala-compiler-2.11.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/plugins/scala-continuations-library_2.11/1.0.2/scala-continuations-library_2.11-1.0.2.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/plugins/scala-continuations-plugin_2.11.1/1.0.2/scala-continuations-plugin_2.11.1-1.0.2.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-library/2.11.1/scala-library-2.11.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-parser-combinators_2.11/1.0.1/scala-parser-combinators_2.11-1.0.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-reflect/2.11.1/scala-reflect-2.11.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-swing_2.11/1.0.1/scala-swing_2.11-1.0.1.jar").append(pathSeparator)
                .append("./libraries/org/scala-lang/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar").append(pathSeparator)
                .append("./libraries/lzma/lzma/0.0.1/lzma-0.0.1.jar").append(pathSeparator)
                .append("./libraries/com/google/guava/guava/16.0/guava-16.0.jar").append(pathSeparator)
                .append("./libraries/com/mojang/realms/1.3.5/realms-1.3.5.jar").append(pathSeparator)
                .append("./libraries/org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar").append(pathSeparator)
                .append("./libraries/org/apache/httpcomponents/httpclient/4.3.3/httpclient-4.3.3.jar").append(pathSeparator)
                .append("./libraries/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar").append(pathSeparator)
                .append("./libraries/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar").append(pathSeparator)
                .append("./libraries/java3d/vecmath/1.3.1/vecmath-1.3.1.jar").append(pathSeparator)
                .append("./libraries/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar").append(pathSeparator)
                .append("./libraries/com/ibm/icu/icu4j-core-mojang/51.2/icu4j-core-mojang-51.2.jar").append(pathSeparator)
                .append("./libraries/net/sf/jopt-simple/jopt-simple/4.5/jopt-simple-4.5.jar").append(pathSeparator)
                .append("./libraries/com/paulscode/codecjorbis/20101023/codecjorbis-20101023.jar").append(pathSeparator)
                .append("./libraries/com/paulscode/codecwav/20101023/codecwav-20101023.jar").append(pathSeparator)
                .append("./libraries/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar").append(pathSeparator)
                .append("./libraries/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar").append(pathSeparator)
                .append("./libraries/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar").append(pathSeparator)
                .append("./libraries/io/netty/netty-all/4.0.10.Final/netty-all-4.0.10.Final.jar").append(pathSeparator)
                .append("./libraries/com/google/guava/guava/15.0/guava-15.0.jar").append(pathSeparator)
                .append("./libraries/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar").append(pathSeparator)
                .append("./libraries/commons-io/commons-io/2.4/commons-io-2.4.jar").append(pathSeparator)
                .append("./libraries/commons-codec/commons-codec/1.9/commons-codec-1.9.jar").append(pathSeparator)
                .append("./libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar").append(pathSeparator)
                .append("./libraries/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar").append(pathSeparator)
                .append("./libraries/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar").append(pathSeparator)
                .append("./libraries/com/mojang/authlib/1.5.21/authlib-1.5.21.jar").append(pathSeparator)
                .append("./libraries/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar").append(pathSeparator)
                .append("./libraries/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar").append(pathSeparator)
                .append("./libraries/org/lwjgl/lwjgl/lwjgl/2.9.1/lwjgl-2.9.1.jar").append(pathSeparator)
                .append("./libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.1/lwjgl_util-2.9.1.jar").append(pathSeparator)
                .append("./libraries/org/lwjgl/lwjgl/lwjgl-platform/2.9.1/lwjgl-platform-2.9.1-natives-linux.jar").append(pathSeparator)
                .append("./libraries/net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-linux.jar").append(pathSeparator)
                .append("./libraries/tv/twitch/twitch/5.16/twitch-5.16.jar ");
        
        args.append("net.minecraft.launchwrapper.Launch ");
        
        args.append("--username ").append(username).append(" ");
        args.append("--accessToken \"").append(session).append("\" ");
        args.append("--version ").append(forgeVersion).append(" ");
        args.append("--gameDir \"").append(gameDir).append("\" ");
        args.append("--assetsDir \"").append(gameDir).append(separator).append("assets\" ");
        args.append("--assetIndex ").append(forgeVersion).append(" ");
        args.append("--userProperties {} ");
        args.append("--tweakClass cpw.mods.fml.common.launcher.FMLTweaker");

        return args.toString();
    }
}
