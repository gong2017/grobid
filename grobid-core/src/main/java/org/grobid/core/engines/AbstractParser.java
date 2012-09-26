package org.grobid.core.engines;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.chasen.crfpp.Tagger;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParser implements Closeable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractParser.class);

	protected Tagger tagger;

	protected AbstractParser(GrobidModels model) {
		tagger = createTagger(model);
	}

	public Tagger getTagger() {
		return tagger;
	}

	// TODO: VZ: Switch to String iterables

	protected void feedTaggerAndParse(StringTokenizer st) {
		feedTaggerAndParse(tagger, st);
	}

	protected void feedTagger(StringTokenizer st) {
		feedTagger(tagger, st);
	}

	public static void feedTaggerAndParse(Tagger tagger, StringTokenizer st) {
		tagger.clear();
		feedTagger(tagger, st);
		if (!tagger.parse()) {
			throw new GrobidException("CRF++ parsing failed.");
		}

		if (!tagger.what().isEmpty()) {
			LOGGER.warn("CRF++ Tagger Warnings: " + tagger.what());
		}
	}

	public static void feedTagger(Tagger tagger, StringTokenizer st) {
		while (st.hasMoreTokens()) {
			String piece = st.nextToken();
			if (piece.trim().isEmpty()) {
				continue;
			}
			if (!tagger.add(piece)) {
				LOGGER.warn("CRF++ Tagger Warnings: " + tagger.what());
				throw new GrobidException("Cannot add a feature row: " + piece
						+ "\n Reason: " + tagger.what());
			}
		}
	}

	public static Tagger createTagger(GrobidModels model) {
		/*File modelPath = GrobidProperties.getInstance().getModelPath(model);
        //featureFactory = FeatureFactory.getInstance();

        if (!modelPath.exists()) {
            throw new RuntimeException("The file path to the " + model.name() + " CRF model is invalid: " + modelPath.getAbsolutePath());
        }
        String cmd = "-m " + modelPath.getAbsolutePath() + " ";
        LOGGER.info("Parameters to CRF++ tagger for model {}: '{}'", model.name(), cmd);
        Tagger tagger;
        try {
            tagger = new Tagger(cmd);
        } catch (NoClassDefFoundError e) {
            throw new GrobidException("Cannot instantiate a tagger for command '" + cmd + "'.");
        }

        return tagger;*/
		return ModelMap.getTagger(model);
	}

	@Override
	public void close() throws IOException {
		if (tagger != null) {
			tagger.clear();
			tagger.delete();
		}
		tagger = null;
	}
}