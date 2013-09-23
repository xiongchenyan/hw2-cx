package edu.cmu.cs.lti.cx.annotators;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

public class QuestionAnswerReader extends JCasAnnotator_ImplBase {

	public QuestionAnswerReader() {
		// Nothing to do here
		}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		//read raw text from aJCas, split question and answers from it.
		String RawText = aJCas.getDocumentText();
		
		String[] lines = RawText.split("\n");
		int p = 0;
		for (String line : lines) {
			
			if (line.startsWith("Q ")) {
				Question question = new Question(aJCas);
				question.setBegin(p + 2);
				question.setEnd(p + line.length());
				question.addToIndexes();				
			}
			else {
				Answer answer = new Answer(aJCas);
				answer.setBegin(p + 4);
				answer.setEnd(p + line.length());
				answer.addToIndexes();
				
				answer.setIsCorrect(line.substring(2,3).equals("1"));
			}			
			p += line.length() + 1;			
		}
		return;
	}

}
