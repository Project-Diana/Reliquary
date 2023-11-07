package xreliquary.integration;

import org.apache.logging.log4j.Level;

import lib.enderwizards.sandstone.mod.ModIntegration;
import xreliquary.Reliquary;

public class NEIModIntegration extends ModIntegration {

    public NEIModIntegration() {
        super("NotEnoughItems");
    }

    @Override
    public void onLoad(boolean status) {
        if (!status) {
            return;
        }

        Reliquary.LOGGER.log(Level.INFO, "Hey NEI! I got a plugin for you! (hopefully in the near future).");
    }

}
