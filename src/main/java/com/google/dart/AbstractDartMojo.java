package com.google.dart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractDartMojo extends AbstractMojo {

	/**
	 * The directory to run the compiler from if fork is true.
	 *
	 * @since 1.0
	 */
	@Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    protected File basedir;

    /**
     * @since 2.0.5
     */
    @Parameter(defaultValue = "src/main/dart", required = true)
    protected File sourceDirectory;

    /**
     * Where to find packages, that is, "package:..." imports.
     *
     * @since 2.0
     */
    @Parameter(defaultValue = "packages", property = "dart.packagePath")
    protected String packagePath;

	/**
	 * provide a dart-sdk
	 *
	 * @since 2.0.0
	 */
	@Parameter(required = true, defaultValue = "${env.DART_SDK}")
    protected File dartSdk;

	protected void checkDartSdk() {

		if (getLog().isDebugEnabled()) {
			getLog().debug("Check for DART_SDK.");
		}

		if (dartSdk == null) {
			throw new NullPointerException("Dart-sdk required. Configuration error for dartSdk?");
		}
		if (!dartSdk.isDirectory()) {
			throw new IllegalArgumentException("Dart-sdk required. Configuration error for dartSdk? dartSdk="
					+ dartSdk.getAbsolutePath());
		}
		getLog().info("Dart-sdk configured to " + dartSdk);
		getLog().info("Version: " + readDartVersion());

	}

    protected String readDartVersion() {
		File dartVersionFile = new File(dartSdk, "version");
		if (!dartVersionFile.isFile()) {
			throw new IllegalArgumentException("Dart version file missing. Configuration error for dartSdk? dartSdk="
					+ dartSdk.getAbsolutePath());
		}
		try (BufferedReader in = new BufferedReader(new FileReader(dartVersionFile))) {
			final String dartVersion = in.readLine();
			if (StringUtils.isEmpty(dartVersion)) {
				throw new NullPointerException("Unable to read dart version. Configuration error for dartSdk?");
			}
			return dartVersion;
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read dart version. Configuration error for dartSdk?", e);
		}
	}

    protected String buildPackagePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(new File(sourceDirectory, packagePath).getAbsolutePath());
        sb.append(File.separator);
        return sb.toString();
    }

	protected String relativePath(final File absolutePath) {
		return absolutePath.getAbsolutePath().replace(basedir + "/", "");
	}

}
