package ru.runa.wf.service;

import java.io.IOException;
import java.util.Collection;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WfScriptServiceTestHelper extends WfServiceTestHelper {

    public WfScriptServiceTestHelper(String testClassPrefixName) throws Exception {
        super(testClassPrefixName);
    }

    public boolean isAllowedToExecutor(SecuredObject securedObject, Executor executor, Permission permission)
            throws ExecutorDoesNotExistException, InternalApplicationException {
        Collection<Permission> permissions = authorizationService.getIssuedPermissions(adminUser, executor, securedObject);
        return permissions.contains(permission);
    }

    public boolean isAllowedToExecutorOnDefinition(Permission permission, Executor executor, String processDefinitionName)
            throws InternalApplicationException {
        WfDefinition definition = definitionService.getLatestProcessDefinition(adminUser, processDefinitionName);
        return isAllowedToExecutor(definition, executor, permission);
    }

    public boolean areExecutorsWeaklyEqual(Executor e1, Executor e2) {
        if (e1 == null || e2 == null) {
            return false;
        }
        if (!Objects.equal(e1.getName(), e1.getName())) {
            return false;
        }
        if (!Objects.equal(e1.getDescription(), e1.getDescription())) {
            return false;
        }
        if ((e1 instanceof Actor) && (e2 instanceof Actor)) {
            Actor a1 = (Actor) e1;
            Actor a2 = (Actor) e2;
            if (!Objects.equal(a1.getFullName(), a2.getFullName())) {
                return false;
            }
        }
        return true;

    }

    public void executeScript(String resourceName)
            throws IOException, ExecutorDoesNotExistException, AuthenticationException, AuthorizationException {
        Delegates.getScriptingService().executeAdminScript(adminUser, readBytesFromFile(resourceName), Maps.<String, byte[]>newHashMap());
    }

    public WfProcess startProcessInstance(String processDefinitionName, Executor performer) throws InternalApplicationException {
        Collection<Permission> validPermissions = Lists.newArrayList(Permission.START, Permission.READ, Permission.READ_PROCESS);
        getAuthorizationService().setPermissions(adminUser, performer.getId(), validPermissions,
                getDefinitionService().getLatestProcessDefinition(adminUser, processDefinitionName));
        getExecutionService().startProcess(adminUser, processDefinitionName, null);
        return getExecutionService().getProcesses(adminUser, getProcessInstanceBatchPresentation()).get(0);
    }

}
