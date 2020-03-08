"""
Where you put definition business logic.
We combine different data sources as available to deliver the best definitions.
"""
import logging
from multiprocessing.context import TimeoutError as MultiprocessingTimeoutError
from multiprocessing.dummy import Pool

from language_service.cache import cache, WEEK
from language_service.service.definition.language.chinese import get_chinese_definitions
from language_service.service.definition.language.english import get_english_definitions
from language_service.service.definition.language.spanish import get_spanish_definitions

logger = logging.getLogger("LanguageService.service.definition")


@cache.memoize(WEEK)
def get_definitions(language, word):
    if language == "CHINESE":
        return get_chinese_definitions(word)

    elif language == "ENGLISH":
        return get_english_definitions(word)

    elif language == "SPANISH":
        return get_spanish_definitions(word)

    else:
        raise NotImplementedError("Unknown language requested: %s")


def fetch_definitions(language, word):
    """
    Helper method to handle getting and deserializing a single definition
    Can't be a lambda because it must be picked to work with the thread pool
    """
    definitions = get_definitions(language, word)
    return word, definitions


def get_definitions_for_group(language, words):
    with Pool(10) as pool:
        # First we trigger the lookups in parallel here
        requests = [
            pool.apply_async(fetch_definitions, args=(language, word)) for word in words
        ]

        # Then we get the results
        definitions = []
        for result in requests:
            try:
                # And then either the result is ready, or we time out.
                word, word_definitions = result.get(5)
                if word_definitions is not None:
                    logger.info("Got definitions in %s for %s" % (language, word))
                    definitions.append((word, word_definitions))
                else:
                    logger.error("No definition in %s found for %s" % (language, word))
                    definitions.append((word, None))
            except MultiprocessingTimeoutError:
                logger.error("Definition lookup timed out")
                definitions.append((word, None))
        return definitions
