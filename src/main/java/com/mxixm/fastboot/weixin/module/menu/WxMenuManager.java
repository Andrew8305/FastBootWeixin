package com.mxixm.fastboot.weixin.module.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mxixm.fastboot.weixin.annotation.WxButton;
import com.mxixm.fastboot.weixin.controller.invoker.WxApiInvokeSpi;
import com.mxixm.fastboot.weixin.exception.WxApiResultException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class WxMenuManager implements ApplicationListener<ApplicationReadyEvent> {

    private static final Log logger = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    private WxApiInvokeSpi wxApiInvokeSpi;

    private Map<WxButton.Group, WxButtonItem> mainButtonLookup = new HashMap<>();

    private MultiValueMap<WxButton.Group, WxButtonItem> groupButtonLookup = new LinkedMultiValueMap<>();

    private Map<String, WxButtonItem> buttonKeyLookup = new HashMap<>();

    private List<WxButtonItem> buttons = new ArrayList<>();

    private WxButtonEventKeyStrategy wxButtonEventKeyStrategy = new WxButtonEventKeyStrategy();

    private WxMenu wxMenu;

    private boolean autoCreate;

    public WxMenuManager(WxApiInvokeSpi wxApiInvokeSpi, boolean autoCreate) {
        this.wxApiInvokeSpi = wxApiInvokeSpi;
        this.autoCreate = autoCreate;
    }

    public WxButtonItem add(WxButton wxButton) {
        WxButtonItem buttonItem = WxButtonItem.builder()
                .setGroup(wxButton.group())
                .setType(wxButton.type())
                .setMain(wxButton.main())
                .setOrder(wxButton.order())
                .setKey(wxButtonEventKeyStrategy.getEventKey(wxButton))
                .setMediaId(wxButton.mediaId())
                .setName(wxButton.name())
                .setUrl(wxButton.url()).build();
        if (wxButton.main()) {
            Assert.isNull(mainButtonLookup.get(wxButton.group()), String.format("已经存在该分组的主菜单，分组是%s", wxButton.group()));
            mainButtonLookup.put(wxButton.group(), buttonItem);
        } else {
            // 可以校验不要超过五个，或者忽略最后的
            groupButtonLookup.add(wxButton.group(), buttonItem);
        }
        if (!StringUtils.isEmpty(wxButton.key())) {
            buttonKeyLookup.put(wxButton.key(), buttonItem);
        }
        buttons.add(buttonItem);
        return buttonItem;
    }

    //有空了改成lambda表达式，先用老循环
    public WxMenu getMenu() {
        if (wxMenu == null) {
            wxMenu = new WxMenu();
            mainButtonLookup.entrySet().stream().sorted(Comparator.comparingInt(e2 -> e2.getKey().ordinal()))
                    .forEach(m -> {
                        groupButtonLookup.getOrDefault(m.getKey(), new ArrayList<>()).stream()
                                .sorted(Comparator.comparingInt(w -> w.getOrder().ordinal()))
                                .forEach(b -> m.getValue().addSubButton(b));
                        wxMenu.add(m.getValue());
                    });
//            try {
//                menuJsonCache = objectMapper.writeValueAsString(wxMenu);
//            } catch (JsonProcessingException e) {
//                logger.error(e.getMessage(), e);
//                // done: 2017/7/25 加入自己的异常体系
//                throw new WxAppException("JSON处理异常", e);
//            }
        }
        return wxMenu;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (!autoCreate) {
            return;
        }
        WxMenus oldWxMenu = null;
        try {
            oldWxMenu = wxApiInvokeSpi.getMenu();
        } catch (WxApiResultException e) {
            // 如果不是菜单不存在，则继续抛出，否则执行创建菜单操作
            if (e.getResultCode() != WxApiResultException.WxApiResultCode.Menu_No_Exist) {
                throw e;
            }
        }
        WxMenu newWxMenu = this.getMenu();
//            WxMenus oldWxMenus = objectMapper.readValue(oldMenuJson, WxMenus.class);
        if (oldWxMenu == null || isMenuChanged(oldWxMenu)) {
            String result = wxApiInvokeSpi.createMenu(newWxMenu);
            logger.info("==============================================================");
            logger.info("            执行创建菜单操作       ");
            logger.info("            操作结果：" + result);
            logger.info("            新的菜单json为：" + newWxMenu);
            logger.info("==============================================================");
        } else {
            logger.info("==============================================================");
            logger.info("            菜单未发生变化             ");
            logger.info("            当前菜单json为：" + oldWxMenu);
            logger.info("==============================================================");
        }
    }

    private boolean isMenuChanged(WxMenus wxMenus) {
        return !this.wxMenu.equals(wxMenus.wxMenu);
    }

    @ToString
    @EqualsAndHashCode
    public static class WxMenu {
        @JsonProperty("button")
        public List<WxButtonItem> mainButtons = new ArrayList<>();

        public void add(WxButtonItem button) {
            mainButtons.add(button);
        }
    }

    @ToString
    public static class WxMenus {

        @JsonProperty("menu")
        public WxMenu wxMenu;

        @JsonProperty("conditionalmenu")
        public List<WxMenu> conditionalWxMenu;
    }

    /**
     * 关于eventKey生成，只有在启用菜单自动生成时才有效，故加在这里面
     */
    private static class WxButtonEventKeyStrategy {

        private Map<String, Integer> nameMap = new HashMap<>();

        public String getEventKey(WxButton wxButton) {
            if (wxButton.type() == WxButton.Type.VIEW) {
                return wxButton.url();
            }
            if (!StringUtils.isEmpty(wxButton.key())) {
                return wxButton.key();
            }
            if (wxButton.main()) {
                return wxButton.group().name();
            } else {
                String key = wxButton.group().name() + "_" + (wxButton.order().ordinal() + 1);
                if (nameMap.containsKey(key)) {
                    int count = nameMap.get(key) + 1;
                    nameMap.put(key, count);
                    return key + "_" + count;
                } else {
                    nameMap.put(key, 1);
                    return key;
                }
            }
        }

    }

}
