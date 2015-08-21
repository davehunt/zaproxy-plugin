package fr.novia.zaproxyplugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.IOException;
import java.util.Map;

@ExportedBean
public class ZAProxyWrapper extends BuildWrapper {

    private final ZAProxy zaproxy;
    private final String zapProxyHost;
    private final int zapProxyPort;

    @DataBoundConstructor
    public ZAProxyWrapper(String zapProxyHost, int zapProxyPort, ZAProxy zaproxy) {
        this.zaproxy = zaproxy;
        this.zapProxyHost = zapProxyHost;
        this.zapProxyPort = zapProxyPort;
        this.zaproxy.setZapProxyHost(zapProxyHost);
        this.zaproxy.setZapProxyPort(zapProxyPort);
    }

    public String getZapProxyHost() {
        return zapProxyHost;
    }

    public int getZapProxyPort() {
        return zapProxyPort;
    }

    public ZAProxy getZaproxy() {
        return zaproxy;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        final ClientApi zapClient = zaproxy.startZAP(build, listener, launcher);

        return new Environment() {

            @Override
            public void buildEnvVars(Map<String, String> env) {
                env.put("ZAP_HOST", zapProxyHost);
                env.put("ZAP_PORT", Integer.toString(zapProxyPort));
            }

            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) {
                try {
                    zaproxy.stopZAP(zapClient, listener);
                } catch (ClientApiException e) {
                    listener.error(ExceptionUtils.getStackTrace(e));
                    return false;
                }
                return true;
            }
        };

    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(ZAProxyWrapper.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Start ZAProxy";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

}
