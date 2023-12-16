package com.swp.fileupload.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSONObject;
import com.swp.fileupload.dto.ContentResultDto;
import com.swp.fileupload.model.LdBucket;
import com.swp.fileupload.model.LdFileRecord;
import com.swp.fileupload.service.LdBucketService;
import com.swp.fileupload.service.LdFileRecordService;
import com.swp.fileupload.util.*;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author user
 * @version $Revision: 1.0 $, $Date: 2021年7月21日 下午4:10:58 $
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
// 允许所有域名访问
@RequestMapping("/fileUpload")
public class LdUploadController {
    @Resource
    private LdBucketService ldBucketService;
    @Resource
    private LdFileRecordService ldFileRecordService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private static final String[] EXTENSIONS =
        {"jpg", "jpeg", "png", "doc", "docx", "xls", "xls", "xlsx", "ppt", "pptx", "pdf", "txt"};
    public static final Set<String> EXTENSION_SET =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(EXTENSIONS)));
    private Logger logger = LogUtils.getBussinessLogger();

    /**
     * 获取token
     *
     * @param ak
     * @param sk
     * @param bucket
     * @return
     */
    @RequestMapping("/getToken")
    public ContentResultDto getToken(String ak, String sk, String bucket) {
//        if (reqData != null && !reqData.isEmpty()) {
//            if (Validator.isEmpty(ak)) ak = reqData.getString("ak");
//            if (Validator.isEmpty(sk)) sk = reqData.getString("sk");
//            if (Validator.isEmpty(bucket)) bucket = reqData.getString("bucket");
//        }
        logger.debug("getToken,ak:{},sk:{},bucket:{}", ak, sk, bucket);
        ContentResultDto dto = new ContentResultDto();
        if (Validator.isEmpty(ak) || Validator.isEmpty(sk) || Validator.isEmpty(bucket)) {
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        boolean isRight = ldBucketService.checkAuth(ak, sk, bucket);
        if (!isRight) {
            dto.setMessage("参数不合法");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }

        String token = UUID.randomUUID().toString(true);
        Long expireTime = System.currentTimeMillis() + (RedisConstants.EXPIRE_ONE_HOUR - 60) * 1000;
        redisTemplate.opsForValue().set(String.format(RedisConstants.KEY_TOKEN_BUCKET, token), bucket,
            RedisConstants.EXPIRE_ONE_HOUR, TimeUnit.SECONDS);
        // JedisUtil.set(String.format(RedisConstants.KEY_TOKEN_BUCKET, token), bucket, RedisConstants.EXPIRE_ONE_HOUR);
        JSONObject result = new JSONObject();
        result.put("token", token);
        result.put("expireTime", expireTime);
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("获取token成功");
        dto.setData(result);
        return dto;
    }

    /**
     * 检查文件是否存在
     *
     * @param request
     * @param fileMd5
     * @param fileName
     * @return
     */
    @RequestMapping("/checkFile")
    public ContentResultDto checkFile(HttpServletRequest request, String fileMd5, String path, String fileName,
                                      String bucket) {
//        if (reqData != null && !reqData.isEmpty()) {
//            if (Validator.isEmpty(fileMd5)) fileMd5 = reqData.getString("fileMd5");
//            if (Validator.isEmpty(path)) path = reqData.getString("path");
//            if (Validator.isEmpty(fileName)) fileName = reqData.getString("fileName");
//            if (Validator.isEmpty(bucket)) bucket = reqData.getString("bucket");
//        }
        logger.debug("checkFile,fileMd5:{},fileName:{},path:{}", fileMd5, fileName, path);
        // ContentResultDto dto = checkToken(request);
        // if (dto != null) {
        // return dto;
        // }
        ContentResultDto dto = new ContentResultDto();
        if (Validator.isEmpty(fileName) || Validator.isEmpty(fileMd5) || Validator.isEmpty(path) || Validator.isEmpty(bucket)) {
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        JSONObject result = new JSONObject();
        List<LdFileRecord> files = ldFileRecordService.getFileByMd5AndName(path + "/" + fileName, fileMd5);
        if (!CollectionUtil.isEmpty(files)) {
            logger.debug("文件已存在,fileMd5:{},fileName:{},path:{},bucket:{}", fileMd5, fileName, path,bucket);
            dto.setMessage("文件已存在");
            dto.setSuccess(Constants.CODE_SUCCESS);
            LdFileRecord file = files.get(0);
            result.put("fileName", file.getFileName());
            result.put("size", file.getSize());
            result.put("fileMd5", fileName);
            result.put("uploadTime", file.getCreationTime());
            result.put("url", file.getUrl());
            dto.setData(result);
            return dto;
        }
        String uploadId = UUID.randomUUID().toString(true);
        Long expireTime = System.currentTimeMillis() + (RedisConstants.EXPIRE_ONE_WEEK - 600) * 1000;
        redisTemplate.opsForValue().set(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId),
                expireTime, RedisConstants.EXPIRE_ONE_WEEK, TimeUnit.SECONDS);
        // JedisUtil.set(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId), String.valueOf(expireTime),
        // RedisConstants.EXPIRE_ONE_WEEK);
        result.put("uploadId", uploadId);
        result.put("expireTime", expireTime);
        dto.setData(result);
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("文件不存在，可继续上传");
        return dto;
    }

    /**
     * 检查分块是否存在，存在的分块，且完整的不需要重新上传了，不完整的返回已上传的大小
     *
     * @param chunk   分块
     * @param fileMd5 文件md5名称
     */
    @RequestMapping(value = "/checkChunk")
    public ContentResultDto checkChunk(HttpServletRequest request, String fileMd5, Integer chunk, Integer chunkSize,
                                       String uploadId, String bucket) {
//        if (reqData != null && !reqData.isEmpty()) {
//            if (Validator.isEmpty(fileMd5)) fileMd5 = reqData.getString("fileMd5");
//            if (chunk == null) chunk = reqData.getInteger("chunk");
//            if (chunk == chunkSize) chunkSize = reqData.getInteger("chunkSize");
//            if (Validator.isEmpty(uploadId)) uploadId = reqData.getString("uploadId");
//            if (Validator.isEmpty(bucket)) bucket = reqData.getString("bucket");
//        }
        // String token = request.getHeader("Authorization");
        logger.debug("checkFile,fileMd5:{},chunk:{},chunkSize:{},uploadId:{}", fileMd5, chunk, chunkSize, uploadId);
        ContentResultDto dto = null;
        // String bucket = JedisUtil.get(String.format(RedisConstants.KEY_TOKEN_BUCKET, token));
        // if (Validators.isEmpty(bucket)) {
        // dto = new ContentResultDto();
        // dto.setMessage("token不合法");
        // dto.setSuccess(Constants.CODE_FAIL);
        // return dto;
        // }
        if (Validator.isEmpty(fileMd5)||Validator.isEmpty(uploadId)||Validator.isEmpty(bucket)){
            dto=new ContentResultDto();
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        dto = checkUploadId(uploadId);
        if (dto != null) {
            return dto;
        }
        dto = new ContentResultDto();
        if (chunk == null) {
            chunk = 0;
        }
        if (chunkSize == null) {
            chunkSize = 0;
        }
        String tempPath = ldBucketService.getTempPath(bucket, DateUtil.formatDate(new Date()));

        String filePath =
            StringUtil.mergeStrings(tempPath, File.separator, fileMd5, File.separator, String.valueOf(chunk));// 合并文件必须要按照顺序合并，所以不能重命名
        File file = new File(filePath);
        // 文件不存在，则返回success，允许上传
        if (file == null || !file.exists()) {
            dto.setSuccess(Constants.CODE_SUCCESS);
            dto.setMessage("分块不存在，允许上传");
            return dto;
        }
        long length = file.length();
        if (length == chunkSize) {
            dto.setSuccess(Constants.CODE_FAIL);
            dto.setMessage("分块已存在，且完整，不用重复上传");
            return dto;
        }
        dto.setData(new JSONObject().fluentPut("chunkSize", length));
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("分块存在，但不完整，继续上传");
        return dto;
    }

    /**
     * 上传文件分块，保存至临时文件夹
     *
     * @param file
     *            文件分块
     * @param fileMd5
     *            文件唯一标志，作为保存分块的文件夹
     * @param chunk
     *            分块索引：1,2,3,4,5....
     *
     * @param isContinua
     *            是否续传
     *
     */
    @RequestMapping(value = "/uploadChunks")
    public ContentResultDto uploadChunks(HttpServletRequest request, MultipartFile file, String fileMd5, Integer chunk,
        String uploadId, boolean isContinua, String bucket) {
        // String token = request.getHeader("Authorization");
        logger.debug("checkFile,fileMd5:{},chunk:{},uploadId:{},isContinua:{}", fileMd5, chunk, uploadId, isContinua);
        ContentResultDto dto = null;
        // String bucket = JedisUtil.get(String.format(RedisConstants.KEY_TOKEN_BUCKET, token));
        // if (Validators.isEmpty(bucket)) {
        // dto = new ContentResultDto();
        // dto.setMessage("token不合法");
        // dto.setSuccess(Constants.CODE_FAIL);
        // return dto;
        // }
        if (Validator.isEmpty(fileMd5)||Validator.isEmpty(uploadId)||Validator.isEmpty(bucket)||file==null||file.isEmpty()){
            dto=new ContentResultDto();
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        dto = checkUploadId(uploadId);
        if (dto != null) {
            return dto;
        }
        dto = new ContentResultDto();

        JSONObject fileInfo = null;
        String tempPath = "";
        try {
            // CommonsMultipartFile cmf = (CommonsMultipartFile) file;
            // FileItem fileItem = cmf.getFileItem();
            tempPath = ldBucketService.getTempPath(bucket, DateUtil.formatDate(new Date()));
            fileInfo = chunksUpload(file, tempPath, fileMd5, chunk == null ? 0 : chunk, isContinua);
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            dto.setMessage("上传文件失败");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String key=String.format(RedisConstants.KEY_LD_FILE_UPLOAD_FOLDER, uploadId);
        // 存分块的临时路径，跨天传输
        redisTemplate.opsForSet().add(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_FOLDER, uploadId),
            StringUtil.mergeStrings(tempPath, "/", fileMd5));
        redisTemplate.expire(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_FOLDER, uploadId), RedisConstants.EXPIRE_ONE_WEEK, TimeUnit.SECONDS);
        logger.debug("chunksUpload,return:" + fileInfo.toJSONString());
        dto.setData(fileInfo);
        dto.setMessage("分块上传成功");
        dto.setSuccess(Constants.CODE_SUCCESS);
        return dto;
    }

    @RequestMapping(value = "/mergeChunks")
    public ContentResultDto mergeChunks(HttpServletRequest request, String fileName, String fileMd5, String path,
                                        String uploadId, String bucket) {
//        if (reqData != null && !reqData.isEmpty()) {
//            if (Validator.isEmpty(fileName)) fileName = reqData.getString("fileName");
//            if (Validator.isEmpty(fileMd5)) fileMd5 = reqData.getString("fileMd5");
//            if (Validator.isEmpty(path)) path = reqData.getString("path");
//            if (Validator.isEmpty(uploadId)) uploadId = reqData.getString("uploadId");
//            if (Validator.isEmpty(bucket)) bucket = reqData.getString("bucket");
//        }
        // String token = request.getHeader("Authorization");
        logger.debug("mergeChunks,fileMd5:{},path:{},fileName:{},uploadId:{}", fileMd5, path, fileName, uploadId);
        ContentResultDto dto = null;
        // String bucket = JedisUtil.get(String.format(RedisConstants.KEY_TOKEN_BUCKET, token));
        // if (Validators.isEmpty(bucket)) {
        // dto = new ContentResultDto();
        // dto.setMessage("token不合法");
        // dto.setSuccess(Constants.CODE_FAIL);
        // return dto;
        // }
        if (Validator.isEmpty(fileName)||Validator.isEmpty(fileMd5)||Validator.isEmpty(path)||Validator.isEmpty(uploadId)||Validator.isEmpty(bucket)){
            dto=new ContentResultDto();
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        dto = checkUploadId(uploadId);
        if (dto != null) {
            return dto;
        }
        dto = new ContentResultDto();
        fileName = getRealName(fileName);
        if (Validator.isEmpty(fileName)) {
            logger.debug("文件名转换失败");
            dto.setMessage("直接上传失败：文件名转换失败");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String fileType = FileUtils.getFileExt(fileName);
        if (Validator.isEmpty(fileType)) {
            logger.debug("合并文件失败：目标文件后缀不能为空");
            dto.setMessage("合并文件失败：目标文件后缀不能为空");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        // 跨天传输待优化
        Set<Object> tempFolders =
            redisTemplate.opsForSet().members(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_FOLDER, uploadId));
        // Set<String> tempFolders = JedisUtil.smembers(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_FOLDER,
        // uploadId));
        List<File> fileList = new ArrayList<File>();
        for (Object tempFolder : tempFolders) {
            File chunkFolder = new File((String)tempFolder);
            File[] fileArray = chunkFolder.listFiles(new FileFilter() {// 排除目录，只要文件
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory();
                }
            });
            if (!ArrayUtil.isAllEmpty(fileArray)) {
                fileList.addAll(Arrays.asList(fileArray));
            }
        }
        if (CollectionUtil.isEmpty(fileList)) {
            dto.setMessage("合并文件失败：分块文件不存在");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        // 排序

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName()) ? -1 : 1;
            }

        });
        logger.debug("分块数量：[{}]", fileList.size());
        LdBucket ldBucket = ldBucketService.getBucketByName(bucket);
        if (bucket == null) {
            dto.setMessage("bucket不存在");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String prePath = ldBucket.getPath();
        // 文件保存的路径
        String targetFolderPath = StringUtil.mergeStrings(prePath, path).replaceAll("//", "/");
        File targetFolder = new File(targetFolderPath);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        String finalFile = StringUtil.mergeStrings(targetFolderPath, "/", fileName);
        File targetFile = new File(finalFile);
        logger.debug("mergeChunks,finalFilePath:{},tempFolders:{}", finalFile, JSONObject.toJSONString(tempFolders));
        if (!mergeFile(fileList, targetFile, tempFolders)) {
            dto.setMessage("合并文件失败：IO异常");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        long fileSize = targetFile.length();
        String url = StringUtil.mergeStrings(ldBucket.getDomain(),
            StringUtil.mergeStrings("/", path, "/", fileName).replaceAll("//", "/"));
        url = URLUtil.encode(url);
        LdFileRecord record = new LdFileRecord(uploadId, bucket, finalFile.replaceAll(prePath,""), url, 0, new Date(), fileMd5, (int)fileSize);
        redisTemplate.delete(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));
        // JedisUtil.remove(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));
        ldFileRecordService.addLdFileRecord(record);
        JSONObject fileInfo = new JSONObject();
        fileInfo.put("fileName", fileName);
        fileInfo.put("size", fileSize);
        fileInfo.put("fileMd5", fileMd5);
        fileInfo.put("uploadTime", new Date());
        // 路径
        fileInfo.put("url", url);
        logger.debug("mergeChunks return data:{}", fileInfo.toJSONString());
        dto.setData(fileInfo);
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("文件上传成功");
        return dto;
    }
    @RequestMapping("/clearCache")
    public void clearCache(){
        ldBucketService.clearCache();
    }

    @RequestMapping("/directUpload")
    public ContentResultDto directUpload(HttpServletRequest request, MultipartFile file, String fileMd5,
        String uploadId, String bucket, String path, String fileName) throws Exception {
        logger.debug("directUpload,fileMd5:{},path:{},fileName:{},uploadId:{},bucket:{}", fileMd5, path, fileName,
            uploadId, bucket);
        ContentResultDto dto = null;
        if (Validator.isEmpty(fileName)||Validator.isEmpty(fileMd5)||Validator.isEmpty(path)||Validator.isEmpty(uploadId)||Validator.isEmpty(bucket)||file==null||file.isEmpty()){
            dto=new ContentResultDto();
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        dto = checkUploadId(uploadId);
        if (dto != null) {
            return dto;
        }
        dto = new ContentResultDto();
        fileName = getRealName(fileName);
        if (Validator.isEmpty(fileName)) {
            logger.debug("文件名转换失败");
            dto.setMessage("直接上传失败：文件名转换失败");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String fileType = FileUtils.getFileExt(fileName);
        if (Validator.isEmpty(fileType)) {
            logger.debug("直接上传失败：目标文件后缀不能为空");
            dto.setMessage("直接上传失败：目标文件后缀不能为空");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }

        LdBucket ldBucket = ldBucketService.getBucketByName(bucket);
        String prePath=ldBucket.getPath();
        // 文件保存的路径
        String targetFolderPath = StringUtil.mergeStrings(prePath, "/", path).replaceAll("//", "/");
        File targetFolder = new File(targetFolderPath);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        String finalFile = StringUtil.mergeStrings(targetFolderPath, "/", fileName);
        File targetFile = new File(finalFile);
        logger.debug("directUpload,finalFilePath:{}", finalFile);
        if (!uploadFile(file, targetFile)) {
            dto.setMessage("文件直传失败，请重试");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String url = StringUtil.mergeStrings(ldBucket.getDomain(),
            StringUtil.mergeStrings("/", path, "/", fileName).replaceAll("//", "/"));
        long fileSize = file.getSize();
        url = URLUtil.encode(url);
        LdFileRecord record = new LdFileRecord(uploadId, bucket, finalFile.replaceAll(prePath,""), url, 0, new Date(), fileMd5, (int)fileSize);
        ldFileRecordService.addLdFileRecord(record);
        // JedisUtil.remove(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));
        redisTemplate.delete(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));

        JSONObject fileInfo = new JSONObject();
        fileInfo.put("fileName", fileName);
        fileInfo.put("size", fileSize);
        fileInfo.put("fileMd5", fileMd5);
        fileInfo.put("uploadTime", new Date());
        // 路径
        fileInfo.put("url", url);
        logger.debug("directUpload return data:{}", fileInfo.toJSONString());
        dto.setData(fileInfo);
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("文件直传成功");
        return dto;
    }

    /**
     * 直传，不需要uploadId
     *
     * @param request
     * @param file
     * @param fileMd5
     * @param bucket
     * @param path
     * @param fileName
     * @return
     * @throws Exception
     */
    @RequestMapping("/directUpload2")
    public ContentResultDto directUpload2(HttpServletRequest request, MultipartFile file, String fileMd5,
                                          String bucket, String path, String fileName) throws Exception {
        logger.debug("directUpload,fileMd5:{},path:{},fileName:{},uploadId:{},bucket:{}", fileMd5, path, fileName,
                bucket);
        ContentResultDto dto = null;
        if (Validator.isEmpty(fileName) || Validator.isEmpty(fileMd5) || Validator.isEmpty(path) || Validator.isEmpty(bucket) || file == null || file.isEmpty()) {
            dto = new ContentResultDto();
            dto.setMessage("参数存在空值");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        dto = new ContentResultDto();
        fileName = getRealName(fileName);
        if (Validator.isEmpty(fileName)) {
            logger.debug("文件名转换失败");
            dto.setMessage("直接上传失败：文件名转换失败");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String fileType = FileUtils.getFileExt(fileName);
        if (Validator.isEmpty(fileType)) {
            logger.debug("直接上传失败：目标文件后缀不能为空");
            dto.setMessage("直接上传失败：目标文件后缀不能为空");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }

        LdBucket ldBucket = ldBucketService.getBucketByName(bucket);
        String prePath = ldBucket.getPath();
        // 文件保存的路径
        String targetFolderPath = StringUtil.mergeStrings(prePath, "/", path).replaceAll("//", "/");
        File targetFolder = new File(targetFolderPath);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        String finalFile = StringUtil.mergeStrings(targetFolderPath, "/", fileName);
        File targetFile = new File(finalFile);
        logger.debug("directUpload,finalFilePath:{}", finalFile);
        if (!uploadFile(file, targetFile)) {
            dto.setMessage("文件直传失败，请重试");
            dto.setSuccess(Constants.CODE_FAIL);
            return dto;
        }
        String url = StringUtil.mergeStrings(ldBucket.getDomain(),
                StringUtil.mergeStrings("/", path, "/", fileName).replaceAll("//", "/"));
        long fileSize = file.getSize();
        url = URLUtil.encode(url);
        String uploadId = UUID.randomUUID().toString(true);
        LdFileRecord record = new LdFileRecord(uploadId, bucket, finalFile.replaceAll(prePath, ""), url, 0, new Date(), fileMd5, (int) fileSize);
        ldFileRecordService.addLdFileRecord(record);


        JSONObject fileInfo = new JSONObject();
        fileInfo.put("fileName", fileName);
        fileInfo.put("size", fileSize);
        fileInfo.put("fileMd5", fileMd5);
        fileInfo.put("uploadTime", new Date());
        // 路径
        fileInfo.put("url", url);
        logger.debug("directUpload return data:{}", fileInfo.toJSONString());
        dto.setData(fileInfo);
        dto.setSuccess(Constants.CODE_SUCCESS);
        dto.setMessage("文件直传成功");
        return dto;
    }


    /**
     * 合并文件操作
     *
     * @param chunkFileList
     *            待合并的文件列表
     * @param targetFile
     *            合并后形成的文件
     * @param tempFolders
     *            待合并的文件列表所在目录
     *
     */
    private boolean mergeFile(List<File> chunkFileList, File targetFile, Set<Object> tempFolders) {
        FileOutputStream oStream = null;
        FileChannel outChannel = null;
        try {
            targetFile.createNewFile();
            // 输出流
            oStream = new FileOutputStream(targetFile);
            outChannel = oStream.getChannel();
            // 合并
            for (File tmp : chunkFileList) {
                FileInputStream inStream = null;
                FileChannel inChannel = null;
                try {
                    inStream = new FileInputStream(tmp);
                    inChannel = inStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } catch (IOException e) {
                    logger.error("mergeFile error", e);
                    return false;
                } finally {
                    try {
                        if (inStream != null) {
                            inStream.close();
                        }
                        if (inChannel != null) {
                            inChannel.close();
                        }
                    } catch (IOException e) {
                        logger.error("mergeFile io error!", e);
                        return false;
                    }
                }
                // 删除分片
                tmp.delete();
            }
            if (outChannel != null) {
                for (Object tempFolder : tempFolders) {
                    // 清除临时文件夹
                    FileUtils.deleteFile(new File((String)tempFolder));
                }
            }
        } catch (IOException e) {
            logger.error("mergeFile io error 1!", e);
            return false;
        } finally {
            try {
                if (oStream != null) {
                    oStream.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                logger.error("io error 2!", e);
                return false;
            }
        }
        return true;
    }

    private ContentResultDto checkToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        ContentResultDto dto = null;
        String bucket = (String)redisTemplate.opsForValue().get(String.format(RedisConstants.KEY_TOKEN_BUCKET, token));
        // String bucket = JedisUtil.get(String.format(RedisConstants.KEY_TOKEN_BUCKET, token));
        if (Validator.isEmpty(bucket)) {
            dto = new ContentResultDto();
            dto.setMessage("token不合法");
            dto.setSuccess(Constants.CODE_FAIL);
        }
        return dto;
    }

    private ContentResultDto checkUploadId(String uploadId) {

        ContentResultDto dto = null;
        // String exp = JedisUtil.get(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));
        Long exp =
                (Long) redisTemplate.opsForValue().get(String.format(RedisConstants.KEY_LD_FILE_UPLOAD_EXPIRE, uploadId));
        if (Validator.isEmpty(exp) || exp <= System.currentTimeMillis()) {
            dto = new ContentResultDto();
            dto.setMessage("上传任务已经过期，请重新上传");
            dto.setSuccess(Constants.CODE_FAIL);
        }
        return dto;
    }

    private JSONObject chunksUpload(MultipartFile fileItem, String path, String fileMd5, int chunk, boolean isContinua)
        throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        String oldName = fileItem.getName();
        logger.debug("chunksUpload,fileMd5:{},chunk:{},path:{},fileName", fileMd5, chunk, path, oldName);
        if (!Validator.isEmpty(oldName) && StringUtil.getRealLength(oldName) > 400) {
            logger.debug("文件名太长,fileName", oldName);
            throw new Exception("文件名太长，最长支持400字符（一个汉字等于两个字符）");
        }

        // 文件类型
        String fileType = FileUtils.getFileExt(oldName);
        // if (Validators.isEmpty(fileType) || (!EXTENSION_SET.contains(fileType) && !EXTENSION_SET.contains(fileType)))
        // {
        // throw new Exception("仅支持zip和rar格式的压缩文件");
        // }

        File dir = new File(StringUtil.mergeStrings(path, File.separator, fileMd5, File.separator));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 合并文件必须要按照顺序合并，所以不能重命名
        String filePath = StringUtil.mergeStrings(path, File.separator, fileMd5, File.separator, String.valueOf(chunk));
        File chunkFile = new File(filePath);
        boolean isExist = chunkFile.exists();
        // 不是续传，且文件已存在，则删除文件
        if (isExist && !isContinua) {
            chunkFile.delete();
        }
        try {
            // 文件已存在，且是续传的，则在文件后面追加
            if (isExist && isContinua) {
                logger.debug("chunksUpload,分片已存在，追加");
                FileOutputStream fout = new FileOutputStream(chunkFile, true);
                fout.write(fileItem.getBytes());
                fout.close();
            } else {
                InputStream inputStream = fileItem.getInputStream();
                org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, chunkFile);
            }

        } catch (Exception e) {
            logger.debug("chunksUpload上传失败");
            throw new Exception("上传文件失败。错误码：ERR01");
        }

        String fileName = FileUtils.getFileName(oldName);
        long fileSize = fileItem.getSize();
        JSONObject fileInfo = new JSONObject();
        fileInfo.put("fileName", fileName);
        fileInfo.put("id", UUID.randomUUID().toString(true));
        fileInfo.put("path", filePath);
        fileInfo.put("size", fileSize);
        fileInfo.put("uploadTime", new Date());
        // fileInfo.put("url", StringUtil.mergeStrings(path, "/", fileName, ".", fileType));
        return fileInfo;
    }

    private boolean uploadFile(MultipartFile fileItem, File targetFile) throws Exception {

        String oldName = fileItem.getName();
        if (!Validator.isEmpty(oldName) && StringUtil.getRealLength(oldName) > 400) {
            logger.debug("文件名太长,fileName", oldName);
            throw new Exception("文件名太长，最长支持400字符（一个汉字等于两个字符）");
        }
        try {
            InputStream inputStream = fileItem.getInputStream();
            org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, targetFile);
        } catch (Exception e) {
            logger.error("uploadFile上传失败", e);
            return false;
        }
        return true;

    }

    private String getRealName(String fileName) {
        String name = null;
        try {
            name = java.net.URLDecoder.decode(fileName, "utf-8");
            if (!Validator.isEmpty(name)) {
                name = name.replaceAll(" ", "");
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("getRealName error", e);
        }
        return name;
    }

}
