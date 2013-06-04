package com.google.dart;

import java.io.File;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

import com.google.dart.util.OsUtil;

/**
 * Goal to invoke the dart pub package manager.
 *
 * @author Daniel Zwicker
 */
@Mojo(name = "pub", defaultPhase = LifecyclePhase.VALIDATE)
public class PubMojo extends AbstractDartMojo {

	private final static String COMMAND_INSTALL = "install";

	private final static String COMMAND_UPDATE = "update";

    // TODO pub publish

    // TODO pub deploy

	/**
	 * Skip the execution of dart2js.
	 *
	 * @since 1.0.3
	 */
	@Parameter(defaultValue = "false", property = "dart.pub.update")
	private boolean update;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        executePub(sourceDirectory);
	}

	protected void executePub(final File sourceDirectory) throws MojoExecutionException {

		if (isPubSkipped()) {
			getLog().info("Updating dependencies (pub packagemanager) is skipped.");
			return;
		}
		String pubPath = null;
		checkPub();
		pubPath = getPubExecutable().getAbsolutePath();

		if (getLog().isDebugEnabled()) {
			getLog().debug("Using pub '" + pubPath + "'.");
		}

		final StreamConsumer output = new WriterStreamConsumer(new OutputStreamWriter(System.out));
		final StreamConsumer error = new WriterStreamConsumer(new OutputStreamWriter(System.err));

		final Commandline cl = new Commandline();
		cl.setExecutable(pubPath);

		cl.createArg().setValue(update ? COMMAND_UPDATE : COMMAND_INSTALL);

		if (getLog().isDebugEnabled()) {
			getLog().debug("Base pub command: " + cl.toString());
		}

		System.out.println();
		System.out.println();

		try {
				getLog().info("Run pub for package root: " + relativePath(sourceDirectory));
				cl.setWorkingDirectory(sourceDirectory);
				if (getLog().isDebugEnabled()) {
					getLog().debug("Execute pub command: " + cl.toString());
				}
				final int returnCode = CommandLineUtils.executeCommandLine(cl, output, error);
				if (getLog().isDebugEnabled()) {
					getLog().debug("pub return code: " + returnCode);
				}
				if (returnCode != 0) {
					throw new MojoExecutionException("Pub returned error code " + returnCode);
				}
		} catch (CommandLineException e) {
			throw new MojoExecutionException("Unable to execute pub", e);
		}

		System.out.println();
		System.out.println();
	}

	protected void checkPub() throws MojoExecutionException {
		checkDartSdk();
		if (!getPubExecutable().canExecute()) {
			throw new MojoExecutionException("Pub not executable! Configuration error for dartSdk? dartSdk="
					+ dartSdk.getAbsolutePath());
		}
	}

	private File getPubExecutable() {
		return new File(dartSdk, "bin/pub" + (OsUtil.isWindows() ? ".bat" : ""));
	}

	public boolean isPubSkipped() {
		return false;
	}
}
