package app.report;

import java.util.Collection;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
public class ESReportPlugin extends AbstractPlugin {
	// @Override
	public String name() {
		return "ESReportPlugin";
	}

	// @Override
	public String description() {
		return "ESReport Plugin Description";
	}
    @Override
	public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = Lists.newArrayList();
        modules.add(ESReportPluginRestModule.class);
        return modules;
    }
}