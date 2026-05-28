package io.github.jxch.platform.example.oracle.cloud.object.storage.s3.example.controller;

import io.github.jxch.platform.oracle.cloud.object.storage.s3.core.OciS3Operations;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/example/ociS3")
public class ExampleController {
    private final OciS3Operations ociS3Operations;

    @GetMapping("/listObjects")
    public List<String> listObjects() {
        List<S3Object> s3Objects = ociS3Operations.listObjects("");
        if (CollectionUtils.isEmpty(s3Objects)) {
            return Collections.emptyList();
        }

        return s3Objects.stream().map(S3Object::key).toList();
    }

}
