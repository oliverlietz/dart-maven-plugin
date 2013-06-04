package com.google.dart;

import java.io.File;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

import com.google.dart.util.OsUtil;

/**
 * Goal to invoke the dart scripts.
 *
 * @author Daniel Zwicker
 */
@Mojo(name = "dart")
public class DartMojo extends AbstractDartMojo {

	/**
	 * Insert runtime type checks and enable assertions (checked mode).
	 *
	 * @since 2.0
	 */
	private final static String ARGUMENT_CHECKED_MODE = "--checked";

	/**
	 * Where to find packages, that is, "package:..." imports.
	 *
	 * @since 2.0
	 */
	protected final static String ARGUMENT_PACKAGE_PATH = "--package-root=";

	/**
	 * enables debugging and listens on specified port for debugger connections
	 * (default port number is 5858)
	 *
	 * @since 2.0
	 */
	private final static String ARGUMENT_DEBUG = "--debug";

	/**
	 * sets a breakpoint at specified location where <location> is one of :
	 * url:<line_num> e.g. test.dart:10
	 * [<class_name>.]<function_name> e.g. B.foo
	 *
	 * @since 2.0
	 */
	private final static String ARGUMENT_BREAK_AT = "--break_at=";

	/**
	 * executes Dart script present in the specified snapshot file
	 *
	 * @since 2.0
	 */
	private final static String ARGUMENT_USE_SCRIPT_SNAPSHOT = "--use_script_snapshot=";

	/**
	 * the Dart script file to run
	 *
	 * @since 2.0
	 */
	@Parameter(property = "script", required = true)
	protected String script;

	/**
	 * Insert runtime type checks and enable assertions (checked mode).
	 *
	 * @since 2.0
	 */
	@Parameter(defaultValue = "false", property = "dart.checkedMode")
	private boolean checkedMode;

	/**
	 * enables debugging and listens on specified port for debugger connections
	 * (default port number is 5858)
	 *
	 * @since 2.0
	 */
	@Parameter(defaultValue = "false", property = "dart.debug")
	private boolean debug;

	/**
	 * enables debugging and listens on specified port for debugger connections
	 * (default port number is 5858)
	 *
	 * @since 2.0
	 */
	@Parameter(property = "dart.debugPort")
	private String debugPort;

	/**
	 * sets a breakpoint at specified location where <location> is one of :
	 * url:<line_num> e.g. test.dart:10
	 * [<class_name>.]<function_name> e.g. B.foo
	 *
	 * @since 2.0
	 */
	@Parameter(property = "dart.breakAt")
	private String breakAt;

	/**
	 * executes Dart script present in the specified snapshot file
	 *
	 * @since 2.0
	 */
	@Parameter(property = "dart.useScriptSnapshot")
	private String useScriptSnapshot;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		executeDart();
	}

	private void executeDart() throws MojoExecutionException {

		final Commandline cl = createBaseCommandline();

        File script = new File(sourceDirectory, this.script);
        getLog().info("Dart script to execute: " + script.getAbsolutePath());

		if (!script.exists() || !script.isFile()) {
			throw new IllegalArgumentException("Script must be a file. scripte=" + script.getAbsolutePath());
		}
		if (!script.canRead()) {
			throw new IllegalArgumentException("Script must be a readable file. scripte=" + script.getAbsolutePath());
		}

		cl.createArg(true).setValue(script.getAbsolutePath());

		final StreamConsumer output = new WriterStreamConsumer(new OutputStreamWriter(System.out));
		final StreamConsumer error = new WriterStreamConsumer(new OutputStreamWriter(System.err));

		getLog().info("Execute dart: " + cl.toString());

		System.out.println();
		System.out.println();

		try {

			final int returnValue = CommandLineUtils.executeCommandLine(cl, output, error);

			if (getLog().isDebugEnabled()) {
				getLog().debug("dart return code: " + returnValue);
			}
			if (returnValue != 0) {
				throw new MojoExecutionException("Dart returned error code " + returnValue);
			}
		} catch (final CommandLineException e) {
			getLog().debug("dart error: ", e);
		}

		System.out.println();
		System.out.println();
	}

	protected Commandline createBaseCommandline() throws MojoExecutionException {

		checkDart();
		String dartPath = getDartExecutable().getAbsolutePath();

		if (getLog().isDebugEnabled()) {
			getLog().debug("Using dart '" + dartPath + "'.");
		}

		final Commandline cl = new Commandline();
		cl.setExecutable(dartPath);

		if (isCheckedMode()) {
			cl.createArg().setValue(ARGUMENT_CHECKED_MODE);
		}

		if (isDebug()) {
			cl.createArg().setValue(ARGUMENT_DEBUG + (isDebugPort() ? ":" + debugPort : ""));
		}

		if (isBreakAt()) {
			cl.createArg().setValue(ARGUMENT_BREAK_AT + breakAt);
		}

		if (isUseScriptSnapshot()) {
			cl.createArg().setValue(ARGUMENT_USE_SCRIPT_SNAPSHOT + useScriptSnapshot);
		}

        cl.createArg().setValue(ARGUMENT_PACKAGE_PATH + buildPackagePath());

		if (getLog().isDebugEnabled()) {
			getLog().debug("Base dart command: " + cl.toString());
		}

		return cl;
	}

	protected void checkDart() throws MojoExecutionException {
		checkDartSdk();
		if (!getDartExecutable().canExecute()) {
			throw new MojoExecutionException("Dart not executable! Configuration error for dartSdk? dartSdk="
					+ dartSdk.getAbsolutePath());
		}
	}

	protected File getDartExecutable() {
		return new File(dartSdk, "bin/dart" + (OsUtil.isWindows() ? ".exe" : ""));
	}

	protected boolean isCheckedMode() {
		return checkedMode;
	}

	protected boolean isDebug() {
		return debug;
	}

	protected boolean isDebugPort() {
		return !StringUtils.isEmpty(debugPort);
	}

	protected boolean isBreakAt() {
		return !StringUtils.isEmpty(breakAt);
	}

	protected boolean isUseScriptSnapshot() {
		return !StringUtils.isEmpty(useScriptSnapshot);
	}
}
