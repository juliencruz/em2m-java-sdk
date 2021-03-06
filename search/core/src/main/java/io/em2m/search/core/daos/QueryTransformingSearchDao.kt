package io.em2m.search.core.daos

import io.em2m.search.core.model.*
import io.em2m.search.core.xform.*
import rx.Observable

class QueryTransformingSearchDao<T>(
        val aliases: Map<String, Field> = emptyMap(),
        val fieldSets: Map<String, List<Field>> = emptyMap(),
        val namedAggs: Map<String, Agg> = emptyMap(),
        delegate: SearchDao<T>) : SearchDaoWrapper<T>(delegate) {

    override fun search(request: SearchRequest): Observable<SearchResult<T>> {
        return super.search(transformRequest(request))
                .map { it.copy(fields = request.fields) }
                .map { transformResult(request, it) }
    }

    override fun count(query: Query): Observable<Long> {
        return super.count(transformQuery(query))
    }

    override fun findOne(query: Query): Observable<T?> {
        return super.findOne(transformQuery(query))
    }

    private fun transformQuery(query: Query, timeZone: String? = null): Query {
        return query
                .let { LuceneQueryTransformer().transform(it) }
                .let { FieldAliasQueryTransformer(aliases).transform(it) }
                .let { NamedAggQueryTransformer(namedAggs, timeZone).transform(it) }
    }

    private fun transformAggs(aggs: List<Agg>): List<Agg> {
        val aliasXform = FieldAliasAggTransformer(aliases)
        val namedXform = NamedAggTransformer(namedAggs)
        return aggs
                .map {
                    aliasXform.transform(it)
                }
                .map {
                    namedXform.transform((it))
                }
    }

    private fun transformSorts(sorts: List<DocSort>): List<DocSort> {
        return sorts.map {
            val alias = aliases[it.field]
            DocSort(alias?.expr ?: alias?.name ?: it.field, it.direction)
        }
    }


    private fun transformRequest(request: SearchRequest): SearchRequest {
        val timeZone: String? = request.params.get("timeZone") as String?
        val fields = request.fields
                .plus(fieldSets[request.fieldSet] ?: emptyList())
                .map {
                    val name = it.name
                    if (name != null) {
                        val alias = aliases[name]
                        if (alias != null) {
                            val label = it.label ?: alias.label
                            val settings = it.settings
                            Field(name = alias.name, expr = alias.expr, label = label, settings = settings)
                        } else it
                    } else it
                }
        val sorts = transformSorts(request.sorts)
        val query = request.query?.let { transformQuery(it, timeZone) }
        val aggs = transformAggs(request.aggs)
        val fieldSet = if (fieldSets.containsKey(request.fieldSet)) null else request.fieldSet
        return request.copy(fieldSet = fieldSet, fields = fields, sorts = sorts, query = query, aggs = aggs)
    }

    private fun transformResult(request: SearchRequest, result: SearchResult<T>): SearchResult<T> {
        val aggs = transformAggResults(request, result.aggs)
        return result.copy(aggs = aggs,  fields = request.fields)
    }

    private fun transformAggResults(request: SearchRequest, aggResults: Map<String, AggResult>): Map<String, AggResult> {
        return request.aggs.mapNotNull { agg ->
            val aggResult = aggResults[agg.key]
            if (agg is NamedAgg) {
                val named = namedAggs[agg.name]
                when (named) {
                    is Fielded -> aggResult?.copy(field = named.field)
                    is FiltersAgg -> aggResult?.copy(buckets = transformFilterBuckets(named, aggResult.buckets))
                    else -> aggResult
                }
            } else aggResult
        }.associateBy { it.key }
    }

    private fun transformFilterBuckets(agg: FiltersAgg, buckets: List<Bucket>?): List<Bucket>? {
        if (buckets == null) return null
        return buckets.map { bucket ->
            val key = bucket.key
            val query = agg.filters[key]
            bucket.copy(query = query)
        }
    }
}