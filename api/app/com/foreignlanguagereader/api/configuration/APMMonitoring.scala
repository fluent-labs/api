package com.foreignlanguagereader.api.configuration

import co.elastic.apm.attach.ElasticApmAttacher

class APMMonitoring {
  ElasticApmAttacher.attach()
}
