package cn.jsonXxxx.elasticsearchDemo.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import cn.jsonXxxx.elasticsearchDemo.util.ESClient;

public class ESTest {

	@Test
	public void test1() throws Exception {
		TransportClient client = ESClient.getClient();
		for (int i = 0; i < 100; i++) {
			IndexRequestBuilder prepareIndex = client.prepareIndex("crm", "user", i + "");
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", String.valueOf(i));
			map.put("name", "易科" + i);
			map.put("age", "18");
			prepareIndex.setSource(map).get();
		}
	}

	@Test
	public void test2() throws Exception {
		TransportClient client = ESClient.getClient();
		HighlightBuilder highlight = new HighlightBuilder().highlighterType("plain").field("name")
				.preTags("<span style=\"color:red\">").postTags("</span>");
		SearchResponse searchResponse = client.prepareSearch("crm").setTypes("user")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(QueryBuilders.matchQuery("name", "易科"))
				.setFrom(0).setSize(10).highlighter(highlight).addSort("id", SortOrder.ASC).get();

		for (SearchHit hit : searchResponse.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			HighlightField hField = hit.getHighlightFields().get("name");
			if (Objects.nonNull(hField)) {
				String nameTmp = "";
				// 将高亮字段放回源数据中替换掉之前的
				for (Text text : hField.fragments()) {
					nameTmp += text;
				}
				source.put("name", nameTmp);
			}
			System.out.println(source);
		}

	}
}
