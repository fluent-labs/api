package com.foreignlanguagereader.api.configuration

import com.google.inject.AbstractModule

class ApiConfigurationModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[APMMonitoring]).asEagerSingleton()
  }
}
