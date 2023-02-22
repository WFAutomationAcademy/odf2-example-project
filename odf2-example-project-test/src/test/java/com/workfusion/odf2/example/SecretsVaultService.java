package com.workfusion.odf2.example;

import com.workfusion.spoke.ControlTower;
import com.workfusion.spoke.ctclient.ApiException;
import com.workfusion.spoke.ctclient.api.SecretsVaultApi;

class SecretsVaultService {

    private final SecretsVaultApi secretsVaultApi;

    SecretsVaultService(SecretsVaultApi secretsVaultApi) {
        this.secretsVaultApi = secretsVaultApi;
    }

    SecretsVaultService(ControlTower controlTower) {
        this(controlTower.getControlTowerApi().getService(SecretsVaultApi.class));
    }

    void setInvoicePlaneCredentials() {
        putOrUpdate("invoice.plane.credentials", "wf-robot@mail.com", "BotsRock4ever!");
    }

    void putOrUpdate(String alias, String key, String value) {
        try {
            secretsVaultApi.putEntryUsingPOST(alias, key, value);
        } catch (ApiException e) {
            // 409 means conflict -> entry already exists -> try to update
            if (e.getCode() == 409) {
                secretsVaultApi.updateEntryUsingPOST(alias, key, value);
            } else {
                throw e;
            }
        }
    }

}
