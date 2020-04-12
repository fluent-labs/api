"""
Where you put definition business logic.
We combine different data sources as available to deliver the best definitions.
"""
import logging
from multiprocessing.context import TimeoutError as MultiprocessingTimeoutError
from multiprocessing.dummy import Pool

from language_service.service.definition.chinese import get_chinese_definitions
from language_service.service.definition.english import get_english_definitions
from language_service.service.definition.spanish import get_spanish_definitions

logger = logging.getLogger("LanguageService.service.definition")


def get_definitions(language, word):
    """
    Main entry point for getting definitions, letting us dispatch to the correct language.
    Why? Each language has different definition sources, so they'll have their
        own combination logic.
    """
    if language == "CHINESE":
        return get_chinese_definitions(word)

    elif language == "ENGLISH":
        return get_english_definitions(word)

    elif language == "SPANISH":
        return get_spanish_definitions(word)

    else:
        raise NotImplementedError("Unknown language requested: %s")


def get_definitions_for_group(language, words):
    """
    Get definitions in parallel for multiple words
    """
    with Pool(10) as pool:
        # First we trigger the lookups in parallel here
        requests = [
            (word, pool.apply_async(get_definitions, args=(language, word)))
            for word in words
        ]

        # Then we get the results
        definitions = []
        for word, result in requests:
            try:
                # And then either the result is ready, or we time out.
                word_definitions = result.get(5)

                if word_definitions is not None:
                    logger.info("Got definitions in %s for %s" % (language, word))
                    definitions.append((word, word_definitions))

                else:
                    logger.error("No definition in %s found for %s" % (language, word))
                    definitions.append((word, None))

            except MultiprocessingTimeoutError:
                logger.error(
                    "Definition lookup in %s timed out for %s" % (language, word)
                )
                definitions.append((word, None))

        return definitions
