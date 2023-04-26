package com.liferay.samples.fbo.glowroot.saml.plugin;

import javax.servlet.http.HttpServletRequest;

import org.glowroot.agent.plugin.api.Agent;
import org.glowroot.agent.plugin.api.MessageSupplier;
import org.glowroot.agent.plugin.api.OptionalThreadContext;
import org.glowroot.agent.plugin.api.TimerName;
import org.glowroot.agent.plugin.api.TraceEntry;
import org.glowroot.agent.plugin.api.checker.Nullable;
import org.glowroot.agent.plugin.api.weaving.BindParameter;
import org.glowroot.agent.plugin.api.weaving.BindReturn;
import org.glowroot.agent.plugin.api.weaving.BindThrowable;
import org.glowroot.agent.plugin.api.weaving.BindTraveler;
import org.glowroot.agent.plugin.api.weaving.OnBefore;
import org.glowroot.agent.plugin.api.weaving.OnReturn;
import org.glowroot.agent.plugin.api.weaving.OnThrow;
import org.glowroot.agent.plugin.api.weaving.Pointcut;
import org.glowroot.agent.plugin.api.weaving.Shim;

public class SAMLAspect {
	
    @Shim("com.liferay.portal.kernel.model.User")
    public interface UserShim {
    	String getEmailAddress();
    	String getScreenName();
    	String getFirstName();
    	String getLastName();
    }
	
	@Pointcut(className = "com.liferay.saml.opensaml.integration.internal.servlet.profile.BaseProfile",
			methodName = "decodeSamlMessage",
            methodParameterTypes = {
            		"javax.servlet.http.HttpServletRequest", 
            		"javax.servlet.http.HttpServletResponse",
            		"com.liferay.saml.opensaml.integration.internal.binding.SamlBinding", 
            		"boolean"},
            timerName = "Decode SAML Message")
    public static class DecodeSAMLMessageAdvice {

        private static final TimerName timer = Agent.getTimerName(DecodeSAMLMessageAdvice.class);

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context, @BindParameter @Nullable HttpServletRequest req) {
        	context.addTransactionAttribute("SAML message", "foo");
        	return context.startTraceEntry(MessageSupplier.create("Decode SAML message"), timer);
        }

        @OnReturn
        public static void onReturn(@BindTraveler TraceEntry traceEntry) {
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
	
	@Pointcut(className = "com.liferay.saml.opensaml.integration.internal.resolver.DefaultUserResolver",
			methodName = "resolveUser",
            methodParameterTypes = {
            		"com.liferay.saml.opensaml.integration.resolver.UserResolver.UserResolverSAMLContext", 
            		"om.liferay.portal.kernel.service.ServiceContext"},
            timerName = "Resolve User")
    public static class ResolveUserAdvice {

        private static final TimerName timer = Agent.getTimerName(ResolveUserAdvice.class);

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context) {
        	return context.startTraceEntry(SAMLMessageSupplier.create("Resolve User"), timer);
        }

        @OnReturn
        public static void onReturn(@BindReturn UserShim user, @BindTraveler TraceEntry traceEntry) {
        	SAMLMessageSupplier samlMessageSupplier = ((SAMLMessageSupplier)traceEntry.getMessageSupplier());
        	samlMessageSupplier.setEmailAddress(user.getEmailAddress());
        	samlMessageSupplier.setScreenName(user.getScreenName());
        	samlMessageSupplier.setFirstName(user.getFirstName());
        	samlMessageSupplier.setLastName(user.getLastName());
        	traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
	
	@Pointcut(className = "com.liferay.saml.opensaml.integration.internal.servlet.profile.WebSsoProfileImpl",
			methodName = "processResponse",
            methodParameterTypes = {
            		"javax.servlet.http.HttpServletRequest", 
            		"javax.servlet.http.HttpServletResponse"},
            timerName = "Process SAML Response")
    public static class ProcessSAMLResponseAdvice {

        private static final TimerName timer = Agent.getTimerName(ProcessSAMLResponseAdvice.class);

        @OnBefore
        public static TraceEntry onBefore(OptionalThreadContext context) {
        	context.setTransactionOuter();
        	MessageSupplier samlMessageSupplier = SAMLMessageSupplier.create("Process SAML Response");
        	return context.startTransaction("SAML", "Process SAML Response", samlMessageSupplier, timer);
        }

        @OnReturn
        public static void onReturn(@BindTraveler TraceEntry traceEntry) {
            traceEntry.end();
        }

        @OnThrow
        public static void onThrow(@BindThrowable Throwable throwable,
                @BindTraveler TraceEntry traceEntry) {
            traceEntry.endWithError(throwable);
        }
    }
}
