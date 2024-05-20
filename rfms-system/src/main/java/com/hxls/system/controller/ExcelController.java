package com.hxls.system.controller;

import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.vo.SysUserGysExcelVO;
import com.hxls.system.config.BaseImageUtils;
import com.qcloud.cos.transfer.Transfer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/excel")
@CrossOrigin
@Slf4j
public class ExcelController {
//    @Autowired
//    private TransferServiceImpl transferService;

//    private static Map<PicturePosition, String> pictureMap = new HashMap<>();

//    @RequestMapping("/upload")
//    public Object upload(@RequestParam("file") MultipartFile file) {
//        //初始化图片容器
//        pictureMap = new HashMap<>();
//        String fileFormat = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
//        Workbook workbook;
//        try {
//            if (ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
//                workbook = new HSSFWorkbook(file.getInputStream());
//            } else if (ExcelFormatEnum.XLSX.getValue().equals(fileFormat)) {
//                workbook = new XSSFWorkbook(file.getInputStream());
//            } else {
//                throw new ServerException("xxx");
//            }
//            //读取excel所有图片
//            if (ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
//                getPicturesXLS(workbook);
//            } else {
//                getPicturesXLSX(workbook);
//            }
//
//            List<Transfer> transferList = new ArrayList<>();
//
//            Sheet sheet = workbook.getSheetAt(0);
//            int rows = sheet.getLastRowNum();
//            for (int i = 1; i <= rows; i++) {
//                Row row = sheet.getRow(i);
//                // 一个对象 自定义
//                Transfer transfer = new Transfer();
//                // 商品图样
//                if (row.getCell(0) != null){
//                    transfer.setImgs(String.valueOf(pictureMap.get(PicturePosition.newInstance(i, 0))));
//                }
//                // 名称
//                if (row.getCell(1) != null){
//                    transfer.setName(this.getCellValue(row.getCell(1)));
//                }
//                transferList.add(transfer);
//            }
//
//            for (Transfer data : transferList) {
//                // 拿到数据自己操作，该新增还是干嘛
////                transferService.save(data);
//            }
//            return Result.ok();
//        } catch (IOException e) {
////            throw new MyException(ExceptionEnum.FILE_ERROR);
//            throw new ServerException("xxx");
//        }
//    }

    @RequestMapping("/readExcelByUrl")
    public Object readExcelByUrl(@RequestParam("url") String excelUrl) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        // 初始化图片容器
        HashMap<PicturePosition, String> pictureMap = new HashMap<>();


        disableSslVerification();
        // 下载Excel文件到本地临时文件
        File tempFile = downloadFile(excelUrl);

        try (InputStream inputStream = new FileInputStream(tempFile)) {
            Workbook workbook;
            String fileFormat = excelUrl.substring(excelUrl.lastIndexOf('.') + 1);
            try {
                if (ExcelFormatEnum.XLS.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new HSSFWorkbook(inputStream);
                } else if (ExcelFormatEnum.XLSX.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new XSSFWorkbook(inputStream);
                } else {
                    throw new ServerException("Unsupported file format.");
                }

                //读取excel所有图片
                if (ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
                    getPicturesXLS(workbook, pictureMap);
                } else {
                    getPicturesXLSX(workbook, pictureMap);
                }

                List<SysUserGysExcelVO> transferList = new ArrayList<>();


                Sheet sheet = workbook.getSheetAt(0);
                int rows = sheet.getLastRowNum();
                for (int i = 1; i <= rows; i++) {
                    Row row = sheet.getRow(i);
                    SysUserGysExcelVO sysUserGysExcelVO = new SysUserGysExcelVO();
                    if (row.getCell(0) != null) {
                        sysUserGysExcelVO.setSupervisor(this.getCellValue(row.getCell(0)));
                    }
                    if (row.getCell(1) != null) {
                        sysUserGysExcelVO.setRealName(this.getCellValue(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        sysUserGysExcelVO.setIdCard(this.getCellValue(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        sysUserGysExcelVO.setMobile(this.getCellValue(row.getCell(3)));
                    }
                    if (row.getCell(4) != null) {
                        sysUserGysExcelVO.setPostName(this.getCellValue(row.getCell(4)));
                    }
                    if (row.getCell(5) != null) {
                        sysUserGysExcelVO.setAvatar(String.valueOf(pictureMap.get(PicturePosition.newInstance(i, 5))));
                    }
                    if (row.getCell(6) != null) {
                        sysUserGysExcelVO.setImageUrl(String.valueOf(pictureMap.get(PicturePosition.newInstance(i, 6))));
                    }
                    if (row.getCell(7) != null) {
                        sysUserGysExcelVO.setLicensePlate(this.getCellValue(row.getCell(7)));
                    }
                    if (row.getCell(8) != null) {
                        sysUserGysExcelVO.setCarTypeName(this.getCellValue(row.getCell(8)));
                    }
                    if (row.getCell(9) != null) {
                        sysUserGysExcelVO.setEmissionStandardName(this.getCellValue(row.getCell(9)));
                    }

                    transferList.add(sysUserGysExcelVO);
                }

                // 执行数据处理逻辑
                // ...
                for (SysUserGysExcelVO data : transferList) {
                    // 拿到数据自己操作，该新增还是干嘛
                    log.info("数据结果图片：{}",data.toString());
//                transferService.save(data);
                }

                return Result.ok();
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            throw new ServerException("Error reading Excel from URL.");
        }
    }


    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // Always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String fileName = "";
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length());
            }

            InputStream inputStream = httpConn.getInputStream();
            File tempFile = File.createTempFile(fileName, ".tmp");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
    }

    // 在下载文件方法之前添加此代码
    public static void disableSslVerification() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * cell数据格式转换
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell){
        switch (cell.getCellType()) {
            case NUMERIC: // 数字
                //如果为时间格式的内容
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    //注：format格式 yyyy-MM-dd hh:mm:ss 中小时为12小时制，若要24小时制，则把小h变为H即可，yyyy-MM-dd HH:mm:ss
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    return sdf.format(HSSFDateUtil.getJavaDate(cell.
                            getNumericCellValue()));
                } else {
                    return new DecimalFormat("0").format(cell.getNumericCellValue());
                }
            case STRING: // 字符串
                return cell.getStringCellValue();
            case BOOLEAN: // Boolean
                return cell.getBooleanCellValue() + "";
            case FORMULA: // 公式
                return cell.getCellFormula() + "";
            case BLANK: // 空值
                return "";
            case ERROR: // 故障
                return null;
            default:
                return null;
        }
    }
    /**
     * 获取Excel2003的图片
     *
     * @param workbook
     */
    private static void getPicturesXLS(Workbook workbook, HashMap<PicturePosition, String> pictureMap) {
        List<HSSFPictureData> pictures = (List<HSSFPictureData>) workbook.getAllPictures();
        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0);
        if (pictures.size() != 0) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    int pictureIndex = pic.getPictureIndex() - 1;
                    HSSFPictureData picData = pictures.get(pictureIndex);
                    PicturePosition picturePosition = PicturePosition.newInstance(anchor.getRow1(), anchor.getCol1());
                    pictureMap.put(picturePosition, printImg(picData));
                }
            }
        }
    }

    /**
     * 获取Excel2007的图片
     *
     * @param workbook
     */
    private static void getPicturesXLSX(Workbook workbook, HashMap<PicturePosition, String> pictureMap) {
        XSSFSheet xssfSheet = (XSSFSheet) workbook.getSheetAt(0);
        for (POIXMLDocumentPart dr : xssfSheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;
                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    try {
                        XSSFClientAnchor anchor = pic.getPreferredSize();
                        if(anchor != null){
                            CTMarker ctMarker = anchor.getFrom();
                            PicturePosition picturePosition = PicturePosition.newInstance(ctMarker.getRow(), ctMarker.getCol());
                            pictureMap.put(picturePosition, printImg(pic.getPictureData()));
                        }
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            }
        }
    }

    /**
     * 保存图片并返回存储地址
     *
     * @param pic
     * @return
     */
    public static String printImg(PictureData pic) {
        try {
            String filePath = UUID.randomUUID().toString() + "." + pic.suggestFileExtension();
            byte[] data = pic.getData();
            // 将二进制数据转换为 Base64 格式
            String base64String = Base64.getEncoder().encodeToString(data);
            String faceUrl = "";
            faceUrl = BaseImageUtils.base64ToUrl(base64String, "CES/FACE", "tupian");
            return faceUrl;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 图片位置
     * 行row 列 col
     */
    @Data
    public static class PicturePosition {
        private int row;
        private int col;

        public static PicturePosition newInstance(int row, int col) {
            PicturePosition picturePosition = new PicturePosition();
            picturePosition.setRow(row);
            picturePosition.setCol(col);
            return picturePosition;
        }
    }

    /**
     * 枚举excel格式
     */
    public enum ExcelFormatEnum {
        XLS(0, "xls"),
        XLSX(1, "xlsx");

        private final Integer key;
        private final String value;

        ExcelFormatEnum(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}

