package net.ohloh.ohcount4j.detect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.ohloh.ohcount4j.Language;
import net.ohloh.ohcount4j.SourceFile;

public class ExtnSTResolver implements Resolver {

	@Override
	public Language resolve(SourceFile sourceFile, List<String> filenames) throws IOException {
		if (looksLikeSmalltalk(sourceFile.getCharSequence())) {
			return Language.SMALLTALK;
		} else {
			return null;
		}
	}

	@Override
	public Language resolve(SourceFile sourceFile) throws IOException {
		return resolve(sourceFile, new ArrayList<String>());
	}

	@Override
	public boolean canResolve(Language language) {
		// Note that this Resolver never actually returns BINARY.
		// We advertise more than one language to force the Detector to run this Resolver.
		// Otherwise, all *.st files would automatically be detected as SMALLTALK, which
		// is not true.
		if (language == Language.BINARY ||
			language == Language.SMALLTALK) {
			return true;
		} else {
			return false;
		}
	}

	private boolean looksLikeSmalltalk(CharSequence contents) {
		if (assignmentPattern.matcher(contents).find() &&
			blockStartPattern.matcher(contents).find()) {
			return true;
		} else {
			return false;
		}
	}

	private static Pattern assignmentPattern = Pattern.compile(":\\s*=");
	private static Pattern blockStartPattern = Pattern.compile(":\\s*\\[");
}