/**
 * Stanford Core NLP 中文分词器 版本 1.0
 * Stanford Core NLP Analyzer Release 1.0
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由陈晨(stevenchenworking@gmail.com)提供
 * provided by Steven Chen
 * Ref: Stanford Core NLP project
 * [Stanford CoreNLP home page](https://stanfordnlp.github.io/CoreNLP/index.html)
 * [Stanford CoreNLP GitHub page](https://github.com/stanfordnlp/CoreNLP)
 */
package org.analyzer.lucene;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.elasticsearch.SpecialPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SDTokenizer extends Tokenizer {

	private final CharTermAttribute termAtt;
	private final OffsetAttribute offsetAtt;
	private int endPosition;
	private Iterator<String> wordsIter = Collections.emptyIterator();

	private Set<String> ignoreSymbols = new HashSet(Stream.of(("囧 ⊙ ● ○ ⊕ ◎ Θ ⊙ ¤ ㈱ ㊣ ★ ☆ ♀ ◆ ◇ ◣ ◢ ◥ ▲ ▼ △ ▽ ⊿ ◤ ◥ ▂ ▃ " +
			"▄ ▅ ▆ ▇ █ █ ■ ▓ □ 〓 ≡ ╝ ╚ ╔ ╗ ╬ ═ ╓ ╩ ┠ ┨ ┯ ┷ ┏ ┓ ┗ ┛ ┳ ⊥ 『 』 ┌ ┐ └ ┘ ∟ 「 」 ↑ ↓ → ← ↘ ↙ ♀ ♂ ┇ " +
			"┅ ﹉ ﹊ ﹍ ﹎ ╭ ╮ ╰ ╯ *^_^* ^*^ ^-^ ^_^ ^（^ ∵ ∴ ‖ ｜ ｜ ︴ ﹏ ﹋ ﹌ （ ） 〔 〕 【 】 〖 〗 ＠ ： ！ / \\ \" " +
			"_ < > ` , · 。 ≈ { } ~ ～ ( ) _ -『 』 √ $ @ * & # ※ 卐 々 ∞ Ψ ∪ ∩ ∈ ∏ の ℡ ぁ § ∮ ” 〃 ミ 灬 ξ № ∑ ⌒ ξ ζ ω ＊ " +
			"\uE7E7 \uE7F3 ㄨ ≮ ≯ ＋ － × ÷ ＋ － ± ／ ＝ ∫ ∮ ∝ ∞ ∧ ∨ ∑ ∏ ‖ ∠ ≌ ∽ ≤ ≥ ≈ ＜ ＞ じ ☆ ↑ ↓ ⊙ ● ★ ☆ ■ ♀ 『 』 Ψ" +
			" ※ → № ← ㊣ ∑ ⌒ 〖 〗 ＠ ξ ζ ω □ ∮ 〓 ※ ∴ ぷ ∏ 卐 【 】 △ √ ∩ ¤ 々 ♀ ♂ ∞ ① ㄨ ≡ ↘ ↙ ┗ ┛ ╰ ☆ ╮ ① ② ③ ④ ⑤ ⑥ ⑦ ⑧ ⑨ ⑩ " +
			"⑴ ⑵ ⑶ ⑷ ⑸ ⑹ ⑺ ⑻ ⑼ ⑽ ⑾ ⑿ ⒀ ⒁ ⒂ ⒃ ⒄ ⒅ ⒆ ⒇ 丨 丩 丬 丶 丷 丿 乀 乙 乂 乄 乆 乛 亅 亠 亻 冂 冫 冖 凵" +
			"\uE81C \uE81D \uE815 \uE816 \uE817 \uE818 \uE819 \uE81E \uE822 \uE823 \uE82B \uE82C\uE830 \uE831 \uE832 \uE833 \uE836 \uE838 \uE839 \uE83A \uE83B \uE83E \uE848 \uE81A \uE81B" +
			" 、 。 ． ？ ！ ～ ＄ ％ ＠ ＆ ＃ ＊ ? ； ∶ … ¨ ， · ˙ ? ‘ ’ “ ” ” 〃 ‘ ′ 〃 ↑ ↓ ← → ↖ ↗ ↙ ↘ ㊣ ◎ ○ ● ⊕ ⊙ ○ ● △ ▲ ☆ ★ ◇ ◆ □ ■ ▽ ▼ § ￥ 〒" +
			" ￠ ￡ ※ ♀ ♂ α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ σ τ υ φ χ ψ ω C").split(" ")).filter(x -> !(null == x || "".equals(x.trim()))).collect(Collectors.toList()));
   	private PositionIncrementAttribute posIncrAtt;

   	private int increment = 0;
   	private StanfordCoreNLP pipeline;

	public SDTokenizer(){
	    super();

	    offsetAtt = addAttribute(OffsetAttribute.class);
	    termAtt = addAttribute(CharTermAttribute.class);
        posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	}

	private StanfordCoreNLP instance() {
		if (null == pipeline) {
			synchronized (SDTokenizer.class) {
				if (null == pipeline) {
					Properties props = new Properties();
					props.setProperty("annotators", "tokenize, ssplit");
					props.setProperty("tokenize.language", "zh");
					props.setProperty("segment.model", "edu/stanford/nlp/models/segmenter/chinese/ctb.gz");
					props.setProperty("segment.sighanCorporaDict", "edu/stanford/nlp/models/segmenter/chinese");
					props.setProperty("segment.serDictionary", "edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz");
					props.setProperty("segment.sighanPostProcessing", "true");
					props.setProperty("ssplit.boundaryTokenRegex", "[.。]|[!?！？]+");

					SpecialPermission.check();
					pipeline = AccessController.doPrivileged((PrivilegedAction<StanfordCoreNLP>) () -> new StanfordCoreNLP(props));
					return pipeline;
				}
			}
		}
		return pipeline;
	}

	@Override
	public boolean incrementToken() {
		clearAttributes();

		if(wordsIter.hasNext()){
			String word = wordsIter.next();
			int wordLength = word.length();
			posIncrAtt.setPositionIncrement(increment + 1);
			termAtt.append(word);
			termAtt.setLength(wordLength);
            offsetAtt.setOffset(endPosition + 1, endPosition + 1 + wordLength);
			endPosition += wordLength;
			return true;
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		// reset the input content
		endPosition = -1;
		increment = 0;

		List<String> words = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(input)) {
			String temp;
			StringBuilder stringBuilder = new StringBuilder();
			while ((temp = br.readLine()) != null) {
				stringBuilder.append(temp.trim());
			}

			words = instance().process(stringBuilder.toString().trim())
					.get(CoreAnnotations.TokensAnnotation.class)
					.stream().map(x -> x.get(CoreAnnotations.TextAnnotation.class))
					.filter(x -> !ignoreSymbols.contains(x))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			wordsIter = words.iterator();
		}
	}	
	
	@Override
	public final void end() throws IOException {
        super.end();

		// set final offset
		int finalOffset = correctOffset(this.endPosition);
		offsetAtt.setOffset(finalOffset, finalOffset);
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + increment);
	}
}
