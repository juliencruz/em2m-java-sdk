package io.em2m.search.core.xform

import io.em2m.search.core.model.*

class FieldAliasAggTransformer(val aliases: Map<String, Field>) : AggTransformer() {

    private val queryTransformer = FieldAliasQueryTransformer(aliases)

    private fun applyAlias(field: String): String {
        return aliases.getOrElse(field, { null })?.name ?: field
    }

    override fun transformDateHistogramAgg(agg: DateHistogramAgg) = DateHistogramAgg(applyAlias(agg.field), agg.format, agg.interval, agg.offset, agg.timeZone, agg.missing, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformDateRangeAgg(agg: DateRangeAgg) = DateRangeAgg(applyAlias(agg.field), agg.format, agg.timeZone, agg.ranges, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformFiltersAgg(agg: FiltersAgg) = FiltersAgg(agg.filters.mapValues { queryTransformer.transform(it.value) }, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformGeoBoundsAgg(agg: GeoBoundsAgg) = GeoBoundsAgg(applyAlias(agg.field), agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformGeoCentroidAgg(agg: GeoCentroidAgg) = GeoCentroidAgg(applyAlias(agg.field), agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformGeoDistanceAgg(agg: GeoDistanceAgg) = GeoDistanceAgg(applyAlias(agg.field), agg.origin, agg.unit, agg.ranges, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformGeoHashAgg(agg: GeoHashAgg) = GeoHashAgg(applyAlias(agg.field), agg.precision, agg.size, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformHistogramAgg(agg: HistogramAgg) = HistogramAgg(applyAlias(agg.field), agg.interval, agg.offset, agg.key, agg.missing, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformMissingAgg(agg: MissingAgg) = MissingAgg(applyAlias(agg.field), agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformRangeAgg(agg: RangeAgg) = RangeAgg(applyAlias(agg.field), agg.ranges, agg.key, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformStatsAgg(agg: StatsAgg) = StatsAgg(applyAlias(agg.field), agg.key, agg.format, agg.aggs, agg.extensions, agg.minDocCount)
    override fun transformTermsAgg(agg: TermsAgg) = TermsAgg(applyAlias(agg.field), agg.size, agg.key, agg.sort, agg.format, agg.missing, agg.aggs, agg.extensions, agg.minDocCount)
}