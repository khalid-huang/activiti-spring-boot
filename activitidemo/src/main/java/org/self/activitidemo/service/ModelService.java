package org.self.activitidemo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

/** 流程模型管理的接口 */
public interface ModelService {
    /** 新建一个模型 */
    HashMap<String, Object> newModel(String modelName, String description, String key);

    /** 获取所有模型 */
    HashMap<String, Object> modelList();

    /** 获取指定页码的模型 */
    HashMap<String, Object> modelsPage(int pageSize, int pageNumber);

    /** 删除指定模型 */
    HashMap<String, Object> deleteModel(String modelId);

    /** 部署模型 */
    HashMap<String, Object> deployModel(String modelId);

    /** 上传已有xml文件，并生成相应模型*/
    HashMap<String, Object> uploadModel(MultipartFile modelFile);



}
