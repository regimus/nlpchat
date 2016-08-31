package com.foo.nlp;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.type.Channel;
import allbegray.slack.type.History;
import allbegray.slack.type.Message;
import allbegray.slack.webapi.SlackWebApiClient;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import java.util.Date;
import java.util.List;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.jcas.JCas;

/**
 *
 * @author reggieyu
 */
public class SlackChitChat {
    // web api doc: https://api.slack.com/methods
    private static final String SLACK_API_URL = "https://slack.com/api";
    // token is associated with myself and the team
    private static final String SLACK_TOKEN = "";
    
    private static AnalysisEngineDescription d1;
    private static AnalysisEngineDescription d2;

    
    public static void main(String[] args) throws Exception {
        d1 = createEngineDescription(OpenNlpSegmenter.class);
        d2 =createEngineDescription(StanfordParser.class, StanfordParser.PARAM_WRITE_PENN_TREE, true);
        long appStart = (new Date().getTime()/1000);
        
        SlackWebApiClient webApiClient = SlackClientFactory.createWebApiClient(SLACK_TOKEN);
        //webApiClient.postMessage("general", "Hello world!", "Bottobot", false);
        
        Channel generalChannel = webApiClient.joinChannel("#general");
        
        // adjust lastRead to the start of app if we haven't read stuff in a while
        String lastRead = generalChannel.getLast_read();
        if (lastRead == null || appStart > Long.parseLong(lastRead)) {
            lastRead = String.valueOf(appStart);
        }
                
        while (true) {
            String now = String.valueOf(new Date().getTime()/1000);
            History generalHistory = webApiClient.getChannelHistory(generalChannel.getId(), now, lastRead, false, 3, false);
            List<Message> messages = generalHistory.getMessages();

            System.out.println("Messages found: " + messages.size());
            for (int i = messages.size() - 1; i >= 0; i--) {
                printTree(webApiClient, messages.get(i));
            }
            
            Thread.sleep(45 * 1000);
            
            lastRead = now;
        }
    }
    
    private static void printTree(SlackWebApiClient client, Message message) {
        try {
            JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(message.getText());
            jcas.setDocumentLanguage("en");
            SimplePipeline.runPipeline(jcas, d1, d2);
            select(jcas, PennTree.class).forEach(tree -> {
                client.postMessage("#parsed", tree.getPennTree(), "Bottobot", false);
            });
        } catch (UIMAException ue) {
            client.postMessage("#parsed", ue.getMessage(), "Bottobot", false);
        }
    }
}
