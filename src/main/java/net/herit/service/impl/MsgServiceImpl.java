package net.herit.service.impl;

import net.herit.service.MsgService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service("msgSvc")
public class MsgServiceImpl implements MsgService {

    private Logger log = Logger.getLogger(MsgServiceImpl.class);

    public String commandExc(String command) throws Exception {

        /**
         * cmd 명령어 실행
         */
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        StringBuffer successOutput = new StringBuffer(); 	// 성공 스트링 버퍼
        StringBuffer errorOutput = new StringBuffer(); 		// 오류 스트링 버퍼
        StringBuffer output = new StringBuffer(); 			// output 스트링 버퍼
        BufferedReader successBufferReader = null; 			// 성공 버퍼
        BufferedReader errorBufferReader = null; 			// 오류 버퍼
        String msg = null; 									// 메시지

        List<String> cmdList = new ArrayList<String>();

        // 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)
        if (System.getProperty("os.name").indexOf("Windows") > -1) {
            cmdList.add("cmd");
            cmdList.add("/c");
        } else {
            cmdList.add("/bin/sh");
            cmdList.add("-c");
        }

        // 명령어 셋팅
        cmdList.add(command);

        System.out.println(command);

        String[] array = cmdList.toArray(new String[cmdList.size()]);

        log.debug("[/msg/server/list] >> process runtime Excute.");

        for(int i=0; i<array.length; i++) {
            log.debug("> " + array[i]);
            System.out.println("> " + array[i]);
        }

        try {

            // 명령어 실행
            process = runtime.exec(array);

            // shell 실행이 정상 동작했을 경우
            // successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));
            successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

            while ((msg = successBufferReader.readLine()) != null) {
                successOutput.append(msg + System.getProperty("line.separator"));
            }

            // shell 실행시 에러가 발생했을 경우
            // errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));
            errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            while ((msg = errorBufferReader.readLine()) != null) {
                errorOutput.append(msg + System.getProperty("line.separator"));
            }

            // 프로세스의 수행이 끝날때까지 대기
            process.waitFor();

            // shell 실행이 정상 종료되었을 경우
            if (process.exitValue() == 0) {
                log.debug("[/msg/server/list] >> 성공");
                log.debug(successOutput.toString());

                output = successOutput;
            } else {
                // shell 실행이 비정상 종료되었을 경우
                log.debug("[/msg/server/list] >> 비정상 종료");
                log.debug(errorOutput.toString());

                output = errorOutput;
            }

            // shell 실행시 에러가 발생
            if (!errorOutput.toString().isEmpty()) {
                // shell 실행이 비정상 종료되었을 경우
                log.debug("[/msg/server/list] >> 오류");
                log.debug(errorOutput.toString());

                // output.append("\n");
                // output.append(errorOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                process.destroy();
                if (successBufferReader != null) successBufferReader.close();
                if (errorBufferReader != null) errorBufferReader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        String result = output.toString().replaceAll("(\\r)", ""); // \\n은 빼자. 컨트롤러에서 추가 파싱해야한다. 디렉토리 구분으로 버튼 생성해야해서...
        return result;
    }
}
