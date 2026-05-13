package com.ksp.screentimereducer.domain.usecase

import com.ksp.screentimereducer.domain.model.Challenge
import com.ksp.screentimereducer.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChallengesUseCase @Inject constructor(private val repo: ChallengeRepository) {
    operator fun invoke(): Flow<List<Challenge>> = repo.observeAll()
}

class StartChallengeUseCase @Inject constructor(private val repo: ChallengeRepository) {
    suspend operator fun invoke(id: String) = repo.start(id)
}

class CompleteChallengeUseCase @Inject constructor(private val repo: ChallengeRepository) {
    suspend operator fun invoke(id: String): Int = repo.complete(id)
}

class AbandonChallengeUseCase @Inject constructor(private val repo: ChallengeRepository) {
    suspend operator fun invoke(id: String) = repo.abandon(id)
}

class SeedChallengesUseCase @Inject constructor(private val repo: ChallengeRepository) {
    suspend operator fun invoke() = repo.seedDefaults()
}
