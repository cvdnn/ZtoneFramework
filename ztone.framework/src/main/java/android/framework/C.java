/*
 * C.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework;

import android.os.Environment;

import java.io.File;
import java.nio.charset.Charset;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-8-30
 */
public abstract class C {
    public abstract static class properties {
        /* ****************
         * app_config中定义的运行参数
         *
         * ***************
         */
        public static final String device = "device";
        public static final String url_base = "url_base";
        public static final String url_tgt = "url_tgt";
        public static final String url_cas = "url_cas";
        public static final String url_shiro = "url_shiro";
        public static final String me_host = "me_host";
        public static final String me_port = "me_port";
        public static final String me_user_name = "me_user_name";
        public static final String me_password = "me_password";

        public static final String dns_resolve_hosts = "dns_resolve_hosts";

        public static final String is_show_to_console = "is_show_to_console";

        /* ****************
         * runtime_config中定义的参数
         *
         * ***************
         */
        public static final String develop_mode = "develop_mode";
        public static final String need_update_version = "need_update_version";
        public static final String channel_id = "channel_id";
        public static final String app_id = "app_id";
        public static final String app_secret = "app_secret";
        public static final String app_path = "app_path";
        public static final String app_config = "app_config";
        public static final String app_code = "app_code";
        public static final String impl_sign = "sign";
        public static final String pull_filter = "app_pull_filter";
        public static final String event_filter = "app_event_filter";
        public static final String analytics = "analytics";
        public static final String b = "b";
        public static final String c = "c";
        public static final String api = "api";
        public static final String os = "os";
        public static final String process_version = "process_version";

        public static final String uuid = "uuid";

        public static final String status_bar_height = "status_bar_height";
    }

    public abstract static class tag {
        public static final String process_message_exchange = ":MessageExchange";
        public static final String permission_message_exchange = "android.push.MESSAGE_EXCHANGE";

        public static final String action_error_dialog = "android.ERROR_DIALOG";

        public static final String action_entry = "android.action.ENTRY";
        /**
         * 用于双进程守护
         */
        public static final String format_action_app_client = "android.push.%1$s.APP_CLIENT";

        /**
         * 消息传递服务的action
         */
        public static final String format_action_message_exchange = "android.push.%1$s.MESSAGE_EXCHANGE_SERVICE";

        public static final String format_action_me_event = "android.push.%1$s.MESSAGE_EXCHANGE_EVENT";

        public static final String me_state_code = "message_exchage_state_code";
        public static final String me_state_message = "message_exchage_state_message";

        public static final String entity_version = "version_entity";

        /**
         * message exchage
         */
        public static final String me_clazz = "me_clazz";
        public static final String me_flag = "me_flag";
        public static final String me_options = "me_options";
        public static final String me_clean_start = "me_clean_start";
        public static final String me_topic = "me_topic";
        public static final String me_keep_alive_topic = "keep_alive_topic";

        public static final String deamon_flag = "deamon_flag";

        public static final String resource_type_drawable = "drawable";
        public static final String resource_type_string = "string";
        public static final String resource_type_id = "id";
        public static final String resource_type_array = "array";
        public static final String resource_type_raw = "raw";
        public static final String resource_type_layout = "layout";
        public static final String resource_type_anim = "anim";
        public static final String resource_type_dimen = "dimen";

        public static final String token = "token";
        public static final String ticket = "ticket";
        public static final String cert = "cert";
        public static final String app_id = "app_id";
        public static final String action = "method";
        public static final String devid = "devid";
        public static final String t = "t";
        public static final String uid = "uid";
        public static final String sid = "sid";
        public static final String ver = "ver";
        public static final String debug = "debug";
        public static final String sign = "sign";
        public static final String cl = "cl";
        public static final String os = "os";
        public static final String api = "api";

        public static final String to_show_fragment = "to_show_fragment";
        public static final String to_show_fragment_bundle = "to_show_fragment_bundle";

        /**
         * 是否属于独立模块
         */
        public static final String is_exclusived_module = "is_exclusived_module";

        public static final String from = "__from";
    }

    public abstract static class flag {
        public static final int unknow_error = -999;

        public static final int none = 0;

        public static final int off = 0;
        public static final int on = 1;

        public static final int result_none = 0;
        public static final int result_finish = 10;

        public static final int error_connection = -1;
        public static final int error_json_parse = -2;
        public static final int error_login = -6;

        public static final int auto_login = -100;
        public static final int no_login = -101;
        public static final int relogin = -102;

        public static final int update_version = -500;

        public static final int debug = 1;

        public static final int me_start_sevice = 1;
        public static final int me_stop_sevice = 2;
        public static final int me_sevice_reconnection = 3;
        public static final int me_start_foreground = 6;
        public static final int me_stop_foreground = 7;
        public static final int me_check_subscribe = 8;

        public static final int app_daemon = 1;

        public static final int setting_none = 0;
        public static final int setting_confirm = 1;
        public static final int setting_cancel = 2;
    }

    public abstract static class request {

    }

    public abstract static class file {
        public static final String assets = "file:///android_asset/";

        public static final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        public static final String path_cache = "cache";
        public static final String path_download = "download";
        public static final String path_image = "image";
        public static final String path_temp = "temp";
        public static final String path_bin = "bin";
        public static final String path_log = "log";
        public static final String path_crash = "crash";
        public static final String path_mqtt = "mqtt";
        public static final String app_config_from_assets = "test_config.properties";
        public static final String app_config_from_local = "app_config.properties";
        public static final String dns_config = "dns_config.ppm";
        public static final String app_config = "app_config.ppm";
        public static final String runtime_config = "runtime_config.ppm";
        public static final String event_name_mapping = "event_name.ppm";
        public static final String apk_install = "install.apk";

        public static final String shared_prefs_system_config = "system_config";
        public static final String android_system_config = "Android/system/data/security/system_config.properties";
        public static final String app_setting = "app_setting.ppm";

        public static final String mix_suffixes_properties = ".ppm";
        public static final String raw_suffixes_properties = ".properties";

        public static final String block_canary_suffixes = ".boc";

        public static final String suffix_apk_file = ".apk";

        public static final String suffix_temp_file = ".tmp";
    }

    // URL集中类
    public abstract static class url {

    }

    public abstract static class lock {
        public static final byte[] app_list = new byte[0];

    }

    public abstract static class value {
        public static final String empty = "";

        public static final String os = "2";

        public static final String line_separator = System.getProperty("line.separator");

        public static final String default_channel_id = "0000000000000000";
        public static final String default_app_id = "00000000";
        public static final String default_app_code = "capt";
        public static final String default_path_app = "Android/data/com.horizon.app";

        /**
         * android默认uuid
         */
        public static final String android_installtion_id = "9774d56d682e549c";

        /**
         * android默认uuid
         */
        public static final String framework_id = "bUYz7A";

        /**
         * 客户端数据更新间隔时间, 单位秒
         */
        public static final int default_interval_time = 7200;

        public static final String encoding = "utf-8";
        public static final Charset charset_encoding = Charset.forName(encoding);

        public static final int buffer_len = 2048;

        /**
         * 平台所能提供的服务
         */
        public static final int count_living_services = 3;

        public static final String default_message_exchage_client_Id = "CAPT";

        /**
         * Inset in pixels to look for touchable content when the user touches the edge of the screen
         */
        public static final int touch_slop = 16;

        /**
         * 执行指令
         */
        public static final String command_message_exchage_reconnect = "$message:reconnect();";
        public static final String command_message_exchage_disconnect = "$message:disconnect();";

        public static final String empty_json_array = "[]";

        public static final String me_app_project_name = "app";

        public static final int api_origin_version = 3;
    }
}
