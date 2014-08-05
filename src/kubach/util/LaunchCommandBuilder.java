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
        //args.append('"').append(pathToJre).append('"').append(separator);
        args.append("java").append(" ");
        args.append("-Xmx").append(memory).append("M ");
        args.append("-Dfml.ignoreInvalidMinecraftCertificates=true ");
        args.append("-Dfml.ignorePatchDiscrepancies=true ");
        args.append("-Djava.library.path=").append(".").append(separator).append("bin").append(separator).append("natives ");//.append(System.nanoTime()).append(" ");
        args.append("-cp ").append(".").append(separator).append("libraries").append(separator).append("net").append(separator).append("minecraftforge").append(separator).append("minecraftforge").append(separator).append("9.11.1.947").append(separator).append("minecraftforge-9.11.1.947.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("net").append(separator).append("minecraft").append(separator).append("launchwrapper").append(separator).append("1.8").append(separator).append("launchwrapper-1.8.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("ow2").append(separator).append("asm").append(separator).append("asm-all").append(separator).append("4.1").append(separator).append("asm-all-4.1.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("scala-lang").append(separator).append("scala-library").append(separator).append("2.10.2").append(separator).append("scala-library-2.10.2.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("scala-lang").append(separator).append("scala-compiler").append(separator).append("2.10.2").append(separator).append("scala-compiler-2.10.2.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("lzma").append(separator).append("lzma").append(separator).append("0.0.1").append(separator).append("lzma-0.0.1.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("net").append(separator).append("sf").append(separator).append("jopt-simple").append(separator).append("jopt-simple").append(separator).append("4.5").append(separator).append("jopt-simple-4.5.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("paulscode").append(separator).append("codecjorbis").append(separator).append("20101023").append(separator).append("codecjorbis-20101023.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("paulscode").append(separator).append("codecwav").append(separator).append("20101023").append(separator).append("codecwav-20101023.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("paulscode").append(separator).append("libraryjavasound").append(separator).append("20101123").append(separator).append("libraryjavasound-20101123.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("paulscode").append(separator).append("librarylwjglopenal").append(separator).append("20100824").append(separator).append("librarylwjglopenal-20100824.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("paulscode").append(separator).append("soundsystem").append(separator).append("20120107").append(separator).append("soundsystem-20120107.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("argo").append(separator).append("argo").append(separator).append("2.25_fixed").append(separator).append("argo-2.25_fixed.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("bouncycastle").append(separator).append("bcprov-jdk15on").append(separator).append("1.47").append(separator).append("bcprov-jdk15on-1.47.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("google").append(separator).append("guava").append(separator).append("guava").append(separator).append("14.0").append(separator).append("guava-14.0.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("apache").append(separator).append("commons").append(separator).append("commons-lang3").append(separator).append("3.1").append(separator).append("commons-lang3-3.1.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("commons-io").append(separator).append("commons-io").append(separator).append("2.4").append(separator).append("commons-io-2.4.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("net").append(separator).append("java").append(separator).append("jinput").append(separator).append("jinput").append(separator).append("2.0.5").append(separator).append("jinput-2.0.5.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("net").append(separator).append("java").append(separator).append("jutils").append(separator).append("jutils").append(separator).append("1.0.0").append(separator).append("jutils-1.0.0.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("com").append(separator).append("google").append(separator).append("code").append(separator).append("gson").append(separator).append("gson").append(separator).append("2.2.2").append(separator).append("gson-2.2.2.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("lwjgl").append(separator).append("lwjgl").append(separator).append("lwjgl").append(separator).append("2.9.0").append(separator).append("lwjgl-2.9.0.jar").append(pathSeparator);
        args.append(".").append(separator).append("libraries").append(separator).append("org").append(separator).append("lwjgl").append(separator).append("lwjgl").append(separator).append("lwjgl_util").append(separator).append("2.9.0").append(separator).append("lwjgl_util-2.9.0.jar").append(pathSeparator);
        args.append(".").append(separator).append("bin").append(separator).append("minecraft.jar ");
        args.append("net.minecraft.launchwrapper.Launch ");
        args.append("--username ").append(username).append(" ");
        args.append("--session \"").append(session).append("\" ");
        args.append("--version ").append(forgeVersion).append(" ");
        args.append("--gameDir \"").append(gameDir).append("\" ");
        args.append("--assetsDir \"").append(gameDir).append(separator).append("assets\" ");
        args.append("--tweakClass cpw.mods.fml.common.launcher.FMLTweaker");

        return args.toString();
    }
}
