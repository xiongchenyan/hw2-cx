package edu.cmu.cs.lti.cx.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

public class CosineScorer extends JCasAnnotator_ImplBase {

	Integer[] lNGramN;
	Float [] lNGramWeight;
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		lNGramN = (Integer[]) aContext.getConfigParameterValue("ngramn");
		lNGramWeight = (Float [])aContext.getConfigParameterValue("ngramweight");
		
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		//Calculate the language model score as p(q|a)
		
		//get question's tokens
		Question question = JCasUtil.selectSingle(aJCas, Question.class);
		List<NGram> lQNGram = new ArrayList<NGram> (JCasUtil.selectCovered(NGram.class, question));
		List<List<NGram>> llQGram = GetNGrams(lQNGram);
//		System.out.println(question);
//		System.out.println(question.getCoveredText());
//		System.out.println(lQNGram);
//		System.out.println(llQGram);
//		System.out.println("Q's ngram:");
//		for (NGram ngram : lQNGram) {
//			System.out.println(ngram.getCoveredText());
//		}
		
		
		
		for (Answer answer : JCasUtil.select(aJCas,Answer.class)) {
			List<NGram> lANGram = new ArrayList<NGram> (JCasUtil.selectCovered(NGram.class,answer));	
//			System.out.println("A's ngram:");
//			for (NGram ngram : lANGram) {
//				System.out.println(ngram.getCoveredText());
//			}
			List<List<NGram>> llAGram = GetNGrams(lANGram);
//			System.out.println(String.format("q[%s]\na[%s]", question.getCoveredText(),answer.getCoveredText()));
			Double AnswerTotalScore = 0.0;
			for (int i = 0; i < llQGram.size(); i ++)
			{
				AnswerTotalScore += lNGramWeight[i] * Cosine(llQGram.get(i),llAGram.get(i));	
//				System.out.println(String.format("[%d] gram [%.2f], cosine [%.2f]",lNGramN[i],lNGramWeight[i],Cosine(llQGram.get(i),llAGram.get(i))));
				
				
			}
			AnswerScore ansScore = new AnswerScore(aJCas);
			ansScore.setAnswer(answer);
			ansScore.setScore(AnswerTotalScore);
			ansScore.addToIndexes();
		}		
		return;		
	}
	
	private List<List<NGram>> GetNGrams(List<NGram> lNGram)
	{
		List<List<NGram>> llNGram = new ArrayList<List<NGram>>();
		for (Integer n : lNGramN) {
			List<NGram> lThisNGram = new ArrayList<NGram>();
			for (NGram gram : lNGram) {
				if (gram.getElements().size() != n) {
					continue;
				}
				lThisNGram.add(gram);
			}
			llNGram.add(lThisNGram);
		}
		return llNGram;		
	}
	
	
	private <T extends Annotation> Double Cosine(List<T> lA, List<T> lB) {
		Double Res = 0.0;		
		Map<String,Integer> hA = GetAnnotationCounts(lA);
		Map<String,Integer> hB = GetAnnotationCounts(lB);
//		System.out.println(lA);
//		System.out.println(hA);		
		for (Entry<String,Integer> TokenEntry : hA.entrySet()) {
			if (hB.containsKey(TokenEntry.getKey())) {
				Res += TokenEntry.getValue() * hB.get(TokenEntry.getKey());
			}
		}		
		Double LengthA = Length(hA);
		Double LengthB = Length(hB);
		
		return Res/ (LengthA * LengthB);
	}
	
	private Double Length(Map<String,Integer> hT) {
		double res = 0;
		for (Entry<String,Integer> entry : hT.entrySet()) {
			Integer value = entry.getValue();
			res += value * value;
		}
		return res;
	}
	
	private <T extends Annotation> Map<String,Integer> GetAnnotationCounts(List<T> lT) {
		if (lT.isEmpty()){
			return null;
		}
		Map<String, Integer> hAnnotation = new HashMap<String, Integer>();
		for (T key: lT) {
			String text = key.getCoveredText();
			if (!hAnnotation.containsKey(text)) {
				hAnnotation.put(text, 0);
			}			
			hAnnotation.put(text, hAnnotation.get(text) + 1);			
		}		
		return hAnnotation;		
	}

}
