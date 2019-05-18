package org.self.activitidemo.controller.client;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Api(tags = "HelloWorldController", description = "hello world测试")
public class HelloWorldController {
    private static Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

//    @Autowired
//    OrgDataService orgDataService;

    @ApiOperation(value = "getHelloworld测试")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<?> getHelloworld() {
        System.out.println("ns hello world");
        return  ResponseEntity.ok("helloworld");
    }

//    @ApiOperation(value = "测试多租户数据库，使用ren_group表测试，返回全部ren_group表的内容 ")
//    @RequestMapping(value = "/multi-test", method = RequestMethod.GET)
//    public ResponseEntity<?> testMultiTenant(@RequestParam(value = "tenantId", required = false) String tenantId) {
//        HashMap<String, Object> result = orgDataService.retrieveAllGroup();
//        return ResponseEntity.status(HttpStatus.OK).body(result);
//    }
}
