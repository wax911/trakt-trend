package io.wax911.trakt.data.movie.mapper

import com.uwetrottmann.trakt5.entities.TrendingMovie
import io.wax911.trakt.data.arch.mapper.TraktTrendMapper
import io.wax911.trakt.data.movie.datasource.local.transformer.MovieTransformer
import io.wax911.trakt.data.show.datasource.local.ShowDao
import io.wax911.trakt.data.show.entity.ShowEntity

internal class TrendingMovieMapper(
    private val localSource: ShowDao,
    private val searchForImages: suspend (Int?) -> Unit
) : TraktTrendMapper<List<TrendingMovie>, List<ShowEntity>>() {

    /**
     * Creates mapped objects and handles the database operations which may be required to map various objects,
     * called in [retrofit2.Callback.onResponse] after assuring that the response was a success
     *
     * @param source the incoming data source type
     * @return Mapped object that will be consumed by [onResponseDatabaseInsert]
     */
    override suspend fun onResponseMapFrom(source: List<TrendingMovie>): List<ShowEntity> {
        return source.map {
            searchForImages(it.movie?.ids?.tmdb)
            MovieTransformer.transform(it.movie!!).copy(
                watchers = it.watchers
            )
        }
    }

    /**
     * Inserts the given object into the implemented room database,
     * called in [retrofit2.Callback.onResponse]
     *
     * @param mappedData mapped object from [onResponseMapFrom] to insert into the database
     */
    override suspend fun onResponseDatabaseInsert(mappedData: List<ShowEntity>) {
        if (mappedData.isNotEmpty())
            localSource.upsert(mappedData)
    }
}