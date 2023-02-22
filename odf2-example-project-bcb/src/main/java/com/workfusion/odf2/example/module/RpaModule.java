package com.workfusion.odf2.example.module;

import javax.inject.Singleton;

import org.codejargon.feather.Provides;

import com.workfusion.odf2.core.cdi.OdfModule;
import com.workfusion.odf2.core.webharvest.rpa.RpaFactory;
import com.workfusion.odf2.core.webharvest.rpa.RpaRunner;

public class RpaModule implements OdfModule {

    @Provides
    @Singleton
    public RpaRunner rpaRunner(RpaFactory rpaFactory) {
        return rpaFactory.builder()
                .closeOnCompletion(true)
                .startInPrivate(true)
                .blockImages(true)
                .maximizeOnStartup(true)
                .capability("cleanSession", true)
                .build();
    }

}
