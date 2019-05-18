package org.self.activitidemo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.self.activitidemo.constant.Pagination;
import org.self.activitidemo.constant.ResponseConstantManager;
import org.self.activitidemo.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

@Service
public class ModelServiceImpl implements ModelService {
    private static final Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public HashMap<String, Object> newModel(String modelName, String description, String key) {
        HashMap<String, Object> result = new HashMap<>();

        //初始化一个空模型
        Model model = repositoryService.newModel();

        //int revision = 1;

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
//        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(modelName);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        try {
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
            result.put("status", ResponseConstantManager.STATUS_SUCCESS);
            result.put("modelId", id);
        } catch (UnsupportedEncodingException e) {
            result.put("status", ResponseConstantManager.STATUS_FAIL);
            result.put("message", e.toString());
        }
        return result;
    }

    @Override
    public HashMap<String, Object> modelList() {
        HashMap<String, Object> result = new HashMap<>();
        List<Model> models = repositoryService.createModelQuery().orderByCreateTime().asc().list();
        result.put("status", ResponseConstantManager.STATUS_SUCCESS);
        result.put("models", models);
        return result;
    }

    @Override
    public HashMap<String, Object> modelsPage(int pageNumber, int pageSize) {
        HashMap<String, Object> result = new HashMap<>();

        Pagination pagination = new Pagination(pageNumber, pageSize);
        int totalCount = (int) repositoryService.createModelQuery().count();
        int totalPages = (int) Math.ceil(totalCount / pageSize);
        pagination.setRowTotal(totalCount);
        pagination.setPageTotal(totalPages);

        List<Model> models = repositoryService.createModelQuery()
                .orderByCreateTime().asc().listPage(pagination.getStart(), pagination.getEnd());
        pagination.setRows(models);
        result.put("status", ResponseConstantManager.STATUS_SUCCESS);
        result.put("modelsPage", pagination);
        return result;
    }

    @Override
    public HashMap<String, Object> deleteModel(String modelId) {
        HashMap<String, Object> result = new HashMap<>();
        repositoryService.deleteModel(modelId);
        result.put("status", ResponseConstantManager.STATUS_SUCCESS);
        return result;
    }

    @Override
    public HashMap<String, Object> deployModel(String modelId) {
        HashMap<String, Object> result = new HashMap<>();

        //获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            result.put("status", ResponseConstantManager.STATUS_FAIL);
            result.put("message", "模型数据为空，请先设计流程并成功保存，再进行发布。");
            return result;
        }
        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if (model.getProcesses().size() == 0) {
                result.put("status", ResponseConstantManager.STATUS_FAIL);
                result.put("message", "数据模型不符要求，请至少设计一条主线流程。");
            }
            //debug
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
//            System.out.println(new String(bpmnBytes, "UTF-8"));

            //发布流程
            String processName = modelData.getName() + ".bpmn20.xml";
//            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
//                    .name(modelData.getName())
//                    .addString(processName, new String(bpmnBytes, "UTF-8"));
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addBpmnModel(processName, model);

            Deployment deployment = deploymentBuilder.deploy();
            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
        } catch (Exception e) {
            result.put("status", ResponseConstantManager.STATUS_FAIL);
            result.put("message", e.toString());
        }
        return result;
    }

    @Override
    public HashMap<String, Object> uploadModel(MultipartFile modelFile) {
        HashMap<String, Object> result = new HashMap<>();
        InputStreamReader in = null;
        try {
            try {
                boolean validFile = false;
                String fileName = modelFile.getOriginalFilename();
                if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
                    validFile = true;
                    XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                    in = new InputStreamReader(new ByteArrayInputStream(modelFile.getBytes()), "UTF-8");
                    XMLStreamReader xtr = xif.createXMLStreamReader(in);
                    BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

                    if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
//                        notificationManager.showErrorNotification(Messages.MODEL_IMPORT_FAILED,
//                                i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_BPMN_EXPLANATION));
                        result.put("status", ResponseConstantManager.STATUS_FAIL);
                        result.put("message", "数据模型无效，必须有一条主流程");
                    } else {
                        if (bpmnModel.getLocationMap().isEmpty()) {
//                            notificationManager.showErrorNotification(Messages.MODEL_IMPORT_INVALID_BPMNDI,
//                                    i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_BPMNDI_EXPLANATION));
                            result.put("status", ResponseConstantManager.STATUS_FAIL);
                            result.put("message", "locationMap为空");
                        } else {
                            String processName = null;
                            if (StringUtils.isNotEmpty(bpmnModel.getMainProcess().getName())) {
                                processName = bpmnModel.getMainProcess().getName();
                            } else {
                                processName = bpmnModel.getMainProcess().getId();
                            }
                            Model modelData;
                            modelData = repositoryService.newModel();
                            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processName);
                            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
                            modelData.setMetaInfo(modelObjectNode.toString());
                            modelData.setName(processName);

                            repositoryService.saveModel(modelData);

                            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                            ObjectNode editorNode = jsonConverter.convertToJson(bpmnModel);

                            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
//                            System.out.println(new String(bpmnBytes, "UTF-8"));
//                            System.out.println(editorNode);

                            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
                            result.put("status", ResponseConstantManager.STATUS_SUCCESS);
                            result.put("modelId", modelData.getId());
                        }
                    }
                } else {
//                    notificationManager.showErrorNotification(Messages.MODEL_IMPORT_INVALID_FILE,
//                            i18nManager.getMessage(Messages.MODEL_IMPORT_INVALID_FILE_EXPLANATION));
                    result.put("status", ResponseConstantManager.STATUS_FAIL);
                    result.put("message", "后缀名无效");
                    System.out.println("err3");
                }
            } catch (Exception e) {
//                String errorMsg = e.getMessage().replace(System.getProperty("line.separator"), "<br/>");
//                notificationManager.showErrorNotification(Messages.MODEL_IMPORT_FAILED, errorMsg);
                result.put("status", ResponseConstantManager.STATUS_FAIL);
                result.put("message", e.toString());
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
//                    notificationManager.showErrorNotification("Server-side error", e.getMessage());
                    result.put("status", ResponseConstantManager.STATUS_FAIL);
                    result.put("message", e.toString());
                }
            }
        }
        return result;
    }
}
