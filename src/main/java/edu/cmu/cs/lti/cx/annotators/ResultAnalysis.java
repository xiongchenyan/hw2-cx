package edu.cmu.cs.lti.cx.annotators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

//import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;


public class ResultAnalysis extends JCasAnnotator_ImplBase {
	double CollectionPSum = 0.0;
	int DocNum = 0;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		//sort answer by score, and calculate p@n
		List<AnswerScore> lAnswer = new ArrayList<AnswerScore>(JCasUtil.select(aJCas,AnswerScore.class));
		Question question = JCasUtil.selectSingle(aJCas,Question.class);
		double CorrectNum = 0.0;
		for (AnswerScore ans : lAnswer) {
			if (ans.getAnswer().getIsCorrect()) {
				CorrectNum += 1.0;				
			}
		}
		
		Collections.sort(lAnswer, new Comparator<AnswerScore> () {
			public int compare(AnswerScore a, AnswerScore b) {
				return a.getScore() > b.getScore() ? -1 : 1;
			}
		});
		
		int TruePos = 0;
		for (int i = 0; i < CorrectNum; i ++) {
			if (lAnswer.get(i).getAnswer().getIsCorrect()) {
				TruePos += 1;
			}
		}
		
		double PAtN = TruePos / CorrectNum;
		CollectionPSum += PAtN;
		DocNum += 1;
		
		
		//print result
		System.out.println(String.format("Question: %s", question.getCoveredText()));
		for (AnswerScore ans : lAnswer) {
			String label = ans.getAnswer().getIsCorrect()?"+":"-";
			System.out.println(String.format("%s %.2f %s", label,ans.getScore(),ans.getAnswer().getCoveredText()));			
		}
		
		System.out.println(String.format("Precision at %d %.2\n",CorrectNum,PAtN));		
		return;
	}
	
	public void collectionProcessComplete() {
		System.out.println("Average precision " + CollectionPSum / DocNum);
	
	}	

}
