package io.wax911.trakt.data.movie.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import co.anitrend.arch.data.request.callback.RequestCallback
import co.anitrend.arch.data.util.PAGING_CONFIGURATION
import co.anitrend.arch.extension.dispatchers.SupportDispatchers
import co.anitrend.arch.extension.network.SupportConnectivity
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Movies
import io.wax911.trakt.data.arch.controller.policy.OnlineControllerPolicy
import io.wax911.trakt.data.arch.extensions.controller
import io.wax911.trakt.data.movie.mapper.MovieMapper
import io.wax911.trakt.data.movie.source.contract.MoviePagedSource
import io.wax911.trakt.data.show.datasource.local.ShowDao
import io.wax911.trakt.data.show.entity.ShowEntity
import io.wax911.trakt.domain.entities.image.TmdbImage
import io.wax911.trakt.domain.entities.image.enums.ShowImageType
import io.wax911.trakt.domain.models.MediaType
import io.wax911.trakt.domain.entities.shared.ShowWithImage
import io.wax911.trakt.domain.entities.shared.contract.ISharedMediaWithImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class MoviePagedSourceImpl(
    private val localSource: ShowDao,
    private val remoteSource: Movies,
    private val mapper: MovieMapper,
    private val connectivity: SupportConnectivity,
    dispatchers: SupportDispatchers
)  : MoviePagedSource(dispatchers) {

    override suspend fun execute(callback: RequestCallback) {
        val call =
            remoteSource.popular(
                supportPagingHelper.page,
                supportPagingHelper.pageSize,
                Extended.FULL
            )

        val controller =
            mapper.controller(
                OnlineControllerPolicy.create(
                    connectivity
                ),
                dispatchers
            )

        controller(call, callback)
    }

    override fun invoke() = liveData {
        val dataSourceFactory = localSource.getPopular(MediaType.MOVIE)

        val result: DataSource.Factory<Int, ISharedMediaWithImage> = dataSourceFactory.map {
            val show = ShowEntity.transform(it)

            ShowWithImage(
                media = show,
                image = TmdbImage(
                    type = MediaType.MOVIE,
                    imageType = ShowImageType.POSTER,
                    id = it.tmdbId ?: 0
                )
            )
        }

        emitSource(
            result.toLiveData(
                config = PAGING_CONFIGURATION,
                boundaryCallback = this@MoviePagedSourceImpl
            )
        )
    }

    /**
     * Clears data sources (databases, preferences, e.t.c)
     */
    override suspend fun clearDataSource(context: CoroutineDispatcher) {
        withContext(context) {
            localSource.deleteAll(MediaType.MOVIE)
        }
    }
}