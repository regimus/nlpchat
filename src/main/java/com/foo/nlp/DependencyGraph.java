package com.foo.nlp;

import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import org.apache.uima.fit.factory.JCasFactory;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author reggieyu
 */
public class DependencyGraph {
    public static void main(String[] args) throws Exception {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is a test");
        jcas.setDocumentLanguage("en");
        AnalysisEngineDescription d1 = createEngineDescription(OpenNlpSegmenter.class), d2=createEngineDescription(StanfordParser.class, StanfordParser.PARAM_WRITE_PENN_TREE, true);
        SimplePipeline.runPipeline(jcas, d1, d2);
        select(jcas, Token.class).forEach(token -> {
            System.out.println(token.getCoveredText() + " " + token.getPos().getPosValue());
        });
        select(jcas, PennTree.class).forEach(tree -> {
            System.out.println(tree.getPennTree());
        });
    }
}
