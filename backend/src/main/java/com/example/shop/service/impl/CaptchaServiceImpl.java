package com.example.shop.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.dto.CaptchaConfigDTO;
import com.example.shop.dto.CaptchaVerifyDTO;
import com.example.shop.entity.CaptchaConfig;
import com.example.shop.entity.CaptchaRecord;
import com.example.shop.mapper.CaptchaConfigMapper;
import com.example.shop.mapper.CaptchaRecordMapper;
import com.example.shop.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    private static final Logger log = LoggerFactory.getLogger(CaptchaServiceImpl.class);
    
    @Autowired
    private CaptchaConfigMapper configMapper;
    
    @Autowired
    private CaptchaRecordMapper recordMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private CaptchaConfig cachedConfig;
    
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String CAPTCHA_LOCK_PREFIX = "captcha_lock:";
    private static final String CAPTCHA_REFRESH_PREFIX = "captcha_refresh:";
    
    @PostConstruct
    public void init() {
        refreshConfig();
    }
    
    @Override
    public CaptchaConfig getConfig() {
        if (cachedConfig == null) {
            refreshConfig();
        }
        return cachedConfig;
    }
    
    @Override
    public void updateConfig(CaptchaConfigDTO dto) {
        List<CaptchaConfig> configs = configMapper.selectList(null);
        CaptchaConfig config;
        if (configs.isEmpty()) {
            config = new CaptchaConfig();
        } else {
            config = configs.get(0);
            if (configs.size() > 1) {
                for (int i = 1; i < configs.size(); i++) {
                    configMapper.deleteById(configs.get(i).getId());
                }
            }
        }
        config.setCaptchaType(dto.getCaptchaType());
        config.setExpireTime(dto.getExpireTime());
        config.setMaxRetryCount(dto.getMaxRetryCount());
        config.setLockTime(dto.getLockTime());
        config.setRefreshInterval(dto.getRefreshInterval());
        config.setIpLimitCount(dto.getIpLimitCount());
        config.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
        
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        this.cachedConfig = config;
        log.info("验证码配置已更新: enabled={}, type={}", config.getEnabled(), config.getCaptchaType());
    }
    
    @Override
    public boolean isEnabled() {
        CaptchaConfig config = getConfig();
        boolean result = config != null && config.isCaptchaEnabled();
        log.info("检查验证码是否启用: config={}, enabled={}, result={}", config, config != null ? config.getEnabled() : null, result);
        return result;
    }
    
    @Override
    public void checkIpLimit(String ip) {
        // 测试阶段暂时禁用 IP 限流检查
        // CaptchaConfig config = getConfig();
        // if (config == null) return;
        // 
        // LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        // int count = recordMapper.countByIpAfterTime(ip, oneHourAgo);
        // if (count >= config.getIpLimitCount()) {
        //     throw new RuntimeException("请求过于频繁，请稍后再试");
        // }
    }
    
    @Override
    public Map<String, Object> generateCaptcha(String ip) {
        CaptchaConfig config = getConfig();
        log.info("生成验证码: config={}, enabled={}", config, config != null ? config.getEnabled() : null);
        if (config == null || !config.isCaptchaEnabled()) {
            throw new RuntimeException("验证码功能未启用");
        }
        
        String captchaId = IdUtil.simpleUUID();
        String captchaType = config.getCaptchaType();
        
        log.info("生成验证码: captchaId={}, captchaType={}", captchaId, captchaType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaType", captchaType);
        
        String captchaKey;
        switch (captchaType) {
            case "GRAPH":
                captchaKey = generateGraphCaptcha(result);
                break;
            case "SLIDE":
                captchaKey = generateSlideCaptcha(result);
                break;
            case "CLICK_TEXT":
                captchaKey = generateClickTextCaptcha(result);
                break;
            case "CALC":
                captchaKey = generateCalcCaptcha(result);
                break;
            default:
                captchaKey = generateGraphCaptcha(result);
        }
        
        log.info("验证码答案: captchaId={}, captchaKey={}", captchaId, captchaKey);
        
        CaptchaRecord record = new CaptchaRecord();
        record.setCaptchaId(captchaId);
        record.setCaptchaType(captchaType);
        record.setCaptchaKey(captchaKey);
        record.setIp(ip);
        record.setRetryCount(0);
        record.setStatus(0);
        record.setExpireTime(LocalDateTime.now().plusSeconds(config.getExpireTime()));
        recordMapper.insert(record);
        
        try {
            redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaId, 
                captchaKey, 
                config.getExpireTime(), 
                TimeUnit.SECONDS
            );
            log.info("验证码已存储到 Redis: key={}", CAPTCHA_KEY_PREFIX + captchaId);
        } catch (Exception e) {
            log.error("存储验证码到 Redis 失败: {}", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> generateCaptcha(String ip, String type) {
        // 如果指定了类型，使用指定类型生成验证码（用于管理后台预览）
        if (type != null && !type.isEmpty()) {
            return generateCaptchaByType(ip, type);
        }
        // 否则使用配置的类型
        return generateCaptcha(ip);
    }
    
    private Map<String, Object> generateCaptchaByType(String ip, String captchaType) {
        String captchaId = IdUtil.simpleUUID();
        
        log.info("生成验证码（指定类型）: captchaId={}, captchaType={}", captchaId, captchaType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaType", captchaType);
        
        String captchaKey;
        switch (captchaType) {
            case "GRAPH":
                captchaKey = generateGraphCaptcha(result);
                break;
            case "SLIDE":
                captchaKey = generateSlideCaptcha(result);
                break;
            case "CLICK_TEXT":
                captchaKey = generateClickTextCaptcha(result);
                break;
            case "CALC":
                captchaKey = generateCalcCaptcha(result);
                break;
            default:
                captchaKey = generateGraphCaptcha(result);
        }
        
        log.info("验证码答案: captchaId={}, captchaKey={}", captchaId, captchaKey);
        
        CaptchaConfig config = getConfig();
        int expireTime = config != null ? config.getExpireTime() : 300;
        
        CaptchaRecord record = new CaptchaRecord();
        record.setCaptchaId(captchaId);
        record.setCaptchaType(captchaType);
        record.setCaptchaKey(captchaKey);
        record.setIp(ip);
        record.setRetryCount(0);
        record.setStatus(0);
        record.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
        recordMapper.insert(record);
        
        try {
            redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaId, 
                captchaKey, 
                expireTime, 
                TimeUnit.SECONDS
            );
            log.info("验证码已存储到 Redis: key={}", CAPTCHA_KEY_PREFIX + captchaId);
        } catch (Exception e) {
            log.error("存储验证码到 Redis 失败: {}", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean verifyCaptcha(CaptchaVerifyDTO dto, String ip) {
        CaptchaConfig config = getConfig();
        if (config == null || !config.isCaptchaEnabled()) {
            return true;
        }
        
        String captchaId = dto.getCaptchaId();
        log.info("验证验证码: captchaId={}, captchaType={}, captchaCode={}", captchaId, dto.getCaptchaType(), dto.getCaptchaCode());
        
        String lockKey = CAPTCHA_LOCK_PREFIX + ip;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new RuntimeException("验证失败次数过多，请" + config.getLockTime() + "秒后再试");
        }
        
        // 先从 Redis 获取，如果没有则从数据库获取
        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        log.info("尝试从 Redis 获取验证码: key={}", redisKey);
        
        String storedKey = null;
        try {
            storedKey = redisTemplate.opsForValue().get(redisKey);
            log.info("Redis 返回结果: {}", storedKey);
        } catch (Exception e) {
            log.error("从 Redis 获取验证码失败: {}", e.getMessage());
        }
        
        if (storedKey == null) {
            // 从数据库获取作为备用
            log.info("Redis 中未找到，尝试从数据库获取");
            CaptchaRecord record = getRecordByCaptchaId(captchaId);
            if (record == null) {
                log.warn("验证码不存在: captchaId={}", captchaId);
                throw new RuntimeException("验证码不存在，请刷新页面");
            }
            if (record.getExpireTime().isBefore(LocalDateTime.now())) {
                log.warn("验证码已过期: captchaId={}, expireTime={}", captchaId, record.getExpireTime());
                throw new RuntimeException("验证码已过期，请重新获取");
            }
            storedKey = record.getCaptchaKey();
            log.info("从数据库获取验证码: captchaId={}, key={}", captchaId, storedKey);
        } else {
            log.info("从 Redis 获取验证码: captchaId={}, key={}", captchaId, storedKey);
        }
        
        boolean success = false;
        String captchaType = dto.getCaptchaType();
        
        switch (captchaType) {
            case "GRAPH":
            case "CALC":
                success = storedKey.equalsIgnoreCase(dto.getCaptchaCode());
                break;
            case "SLIDE":
                success = verifySlideCaptcha(storedKey, dto.getSliderX());
                break;
            case "CLICK_TEXT":
                success = verifyClickTextCaptcha(storedKey, dto.getClickPoints());
                break;
        }
        
        if (success) {
            redisTemplate.delete(CAPTCHA_KEY_PREFIX + captchaId);
            CaptchaRecord record = getRecordByCaptchaId(captchaId);
            if (record != null) {
                record.setStatus(1);
                recordMapper.updateById(record);
            }
        } else {
            recordMapper.incrementRetryCount(captchaId);
            CaptchaRecord record = getRecordByCaptchaId(captchaId);
            if (record != null && record.getRetryCount() >= config.getMaxRetryCount()) {
                redisTemplate.opsForValue().set(lockKey, "1", config.getLockTime(), TimeUnit.SECONDS);
                record.setStatus(2);
                recordMapper.updateById(record);
            }
        }
        
        return success;
    }
    
    private void refreshConfig() {
        List<CaptchaConfig> configs = configMapper.selectList(null);
        if (configs.isEmpty()) {
            cachedConfig = new CaptchaConfig();
            cachedConfig.setEnabled(0);
            cachedConfig.setCaptchaType("GRAPH");
            cachedConfig.setExpireTime(300);
            cachedConfig.setMaxRetryCount(5);
            cachedConfig.setLockTime(300);
            cachedConfig.setRefreshInterval(60);
            cachedConfig.setIpLimitCount(10);
            configMapper.insert(cachedConfig);
        } else {
            cachedConfig = configs.get(0);
            if (configs.size() > 1) {
                for (int i = 1; i < configs.size(); i++) {
                    configMapper.deleteById(configs.get(i).getId());
                }
            }
        }
    }
    
    private CaptchaRecord getRecordByCaptchaId(String captchaId) {
        LambdaQueryWrapper<CaptchaRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaptchaRecord::getCaptchaId, captchaId);
        return recordMapper.selectOne(wrapper);
    }
    
    private String generateGraphCaptcha(Map<String, Object> result) {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        String code = captcha.text().toLowerCase();
        String base64 = captcha.toBase64();
        result.put("captchaImage", base64);
        return code;
    }
    
    private String generateSlideCaptcha(Map<String, Object> result) {
        int width = 300;
        int height = 180;
        int sliderSize = 60;
        
        int sliderX = RandomUtil.randomInt(sliderSize + 30, width - sliderSize - 30);
        int sliderY = RandomUtil.randomInt(20, height - sliderSize - 20);
        
        try {
            BufferedImage backgroundImage = createSlideBackgroundImage(width, height, sliderX, sliderY, sliderSize);
            BufferedImage sliderImage = createPuzzleSliderImage(sliderSize, sliderX, sliderY, width, height);
            
            String bgBase64 = imageToBase64(backgroundImage);
            String sliderBase64 = imageToBase64(sliderImage);
            
            result.put("backgroundImage", bgBase64);
            result.put("sliderImage", sliderBase64);
            result.put("sliderY", sliderY);
            
            return String.valueOf(sliderX);
        } catch (Exception e) {
            log.error("生成滑动验证码失败", e);
            return generateGraphCaptcha(result);
        }
    }
    
    private String generateClickTextCaptcha(Map<String, Object> result) {
        int width = 280;
        int height = 80;
        
        String[] allChars = {"人", "口", "手", "大", "小", "上", "下", "左", "右", "中", "文", "字", "验", "证", "码"};
        List<String> selectedChars = new ArrayList<>();
        List<int[]> positions = new ArrayList<>();
        
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            String ch;
            do {
                ch = allChars[random.nextInt(allChars.length)];
            } while (selectedChars.contains(ch));
            selectedChars.add(ch);
            
            int x = 30 + random.nextInt(width - 60);
            int y = 20 + random.nextInt(height - 40);
            positions.add(new int[]{x, y});
        }
        
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            g2d.setColor(new Color(200, 200, 200));
            for (int i = 0; i < 50; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                g2d.fillOval(x, y, 2, 2);
            }
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("宋体", Font.BOLD, 22));
            
            for (int i = 0; i < selectedChars.size(); i++) {
                g2d.drawString(selectedChars.get(i), positions.get(i)[0], positions.get(i)[1]);
            }
            
            g2d.dispose();
            
            String base64 = imageToBase64(image);
            result.put("captchaImage", base64);
            result.put("tipText", String.join("、", selectedChars));
            result.put("targetCount", selectedChars.size());
            
            StringBuilder keyBuilder = new StringBuilder();
            for (int[] pos : positions) {
                keyBuilder.append(pos[0]).append(",").append(pos[1]).append(";");
            }
            return keyBuilder.toString();
        } catch (Exception e) {
            return generateGraphCaptcha(result);
        }
    }
    
    private String generateCalcCaptcha(Map<String, Object> result) {
        Random random = new Random();
        int a = random.nextInt(50) + 1;
        int b = random.nextInt(20) + 1;
        int operator = random.nextInt(3);
        
        String expression;
        int answer;
        
        switch (operator) {
            case 0:
                expression = a + " + " + b + " = ?";
                answer = a + b;
                break;
            case 1:
                if (a < b) {
                    int temp = a;
                    a = b;
                    b = temp;
                }
                expression = a + " - " + b + " = ?";
                answer = a - b;
                break;
            default:
                a = random.nextInt(10) + 1;
                b = random.nextInt(10) + 1;
                expression = a + " × " + b + " = ?";
                answer = a * b;
        }
        
        try {
            BufferedImage image = new BufferedImage(150, 48, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRect(0, 0, 150, 48);
            
            g2d.setColor(new Color(100, 100, 100));
            for (int i = 0; i < 30; i++) {
                int x = random.nextInt(150);
                int y = random.nextInt(48);
                g2d.fillOval(x, y, 1, 1);
            }
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString(expression, 10, 32);
            
            g2d.dispose();
            
            String base64 = imageToBase64(image);
            result.put("captchaImage", base64);
            
            return String.valueOf(answer);
        } catch (Exception e) {
            result.put("captchaCode", expression);
            return String.valueOf(answer);
        }
    }
    
    private boolean verifySlideCaptcha(String storedKey, Integer sliderX) {
        if (sliderX == null) return false;
        int targetX = Integer.parseInt(storedKey);
        int diff = Math.abs(sliderX - targetX);
        log.info("滑动验证码验证: 目标位置={}, 用户滑动位置={}, 差值={}", targetX, sliderX, diff);
        // 容差设为 10 像素
        return diff <= 10;
    }
    
    private boolean verifyClickTextCaptcha(String storedKey, List<List<Integer>> clickPoints) {
        if (clickPoints == null || clickPoints.size() != 3) return false;
        
        String[] positions = storedKey.split(";");
        if (positions.length != 3) return false;
        
        for (int i = 0; i < 3; i++) {
            String[] coords = positions[i].split(",");
            if (coords.length != 2) return false;
            
            int targetX = Integer.parseInt(coords[0]);
            int targetY = Integer.parseInt(coords[1]);
            
            int clickX = clickPoints.get(i).get(0);
            int clickY = clickPoints.get(i).get(1);
            
            if (Math.abs(clickX - targetX) > 20 || Math.abs(clickY - targetY) > 20) {
                return false;
            }
        }
        return true;
    }
    
    private BufferedImage createSlideBackgroundImage(int width, int height, int sliderX, int sliderY, int sliderSize) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 创建渐变背景
        Color[] colors = {
            new Color(41, 128, 185),
            new Color(52, 152, 219),
            new Color(44, 62, 80),
            new Color(155, 89, 182),
            new Color(39, 174, 96),
            new Color(22, 160, 133)
        };
        Random random = new Random();
        Color baseColor = colors[random.nextInt(colors.length)];
        
        GradientPaint gradient = new GradientPaint(0, 0, baseColor, width, height, baseColor.darker());
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // 添加装饰图案
        g2d.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(40) + 10;
            g2d.fillOval(x, y, size, size);
        }
        
        // 添加干扰线
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(255, 255, 255, 40));
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制拼图块缺口（半透明遮罩）
        g2d.setColor(new Color(0, 0, 0, 120));
        drawPuzzlePiece(g2d, sliderX, sliderY, sliderSize);
        
        // 绘制拼图块边框
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(3));
        drawPuzzlePieceBorder(g2d, sliderX, sliderY, sliderSize);
        
        g2d.dispose();
        return image;
    }
    
    private BufferedImage createPuzzleSliderImage(int sliderSize, int sliderX, int sliderY, int bgWidth, int bgHeight) {
        BufferedImage image = new BufferedImage(sliderSize, sliderSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制拼图块主体（白色半透明）
        g2d.setColor(new Color(255, 255, 255, 230));
        fillPuzzlePiece(g2d, 0, 0, sliderSize);
        
        // 绘制拼图块边框
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(2.5f));
        drawPuzzlePieceBorderOnly(g2d, 0, 0, sliderSize);
        
        // 添加高光效果
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(1.5f));
        drawPuzzlePieceHighlight(g2d, 0, 0, sliderSize);
        
        g2d.dispose();
        return image;
    }
    
    private void drawPuzzlePiece(Graphics2D g2d, int x, int y, int size) {
        int r = size / 2;
        int tabSize = size / 4;
        
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(x, y);
        
        // 上边 + 凸起
        path.lineTo(x + r - tabSize, y);
        path.curveTo(x + r - tabSize/2, y - tabSize, x + r + tabSize/2, y - tabSize, x + r + tabSize, y);
        path.lineTo(x + size, y);
        
        // 右边 + 凸起
        path.lineTo(x + size, y + r - tabSize);
        path.curveTo(x + size + tabSize, y + r - tabSize/2, x + size + tabSize, y + r + tabSize/2, x + size, y + r + tabSize);
        path.lineTo(x + size, y + size);
        
        // 下边
        path.lineTo(x, y + size);
        
        // 左边
        path.lineTo(x, y);
        
        path.closePath();
        g2d.fill(path);
    }
    
    private void drawPuzzlePieceBorder(Graphics2D g2d, int x, int y, int size) {
        int r = size / 2;
        int tabSize = size / 4;
        
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(x, y);
        
        path.lineTo(x + r - tabSize, y);
        path.curveTo(x + r - tabSize/2, y - tabSize, x + r + tabSize/2, y - tabSize, x + r + tabSize, y);
        path.lineTo(x + size, y);
        
        path.lineTo(x + size, y + r - tabSize);
        path.curveTo(x + size + tabSize, y + r - tabSize/2, x + size + tabSize, y + r + tabSize/2, x + size, y + r + tabSize);
        path.lineTo(x + size, y + size);
        
        path.lineTo(x, y + size);
        path.lineTo(x, y);
        
        path.closePath();
        g2d.draw(path);
    }
    
    private void fillPuzzlePiece(Graphics2D g2d, int x, int y, int size) {
        int r = size / 2;
        int tabSize = size / 4;
        
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(x, y);
        
        path.lineTo(x + r - tabSize, y);
        path.curveTo(x + r - tabSize/2, y - tabSize, x + r + tabSize/2, y - tabSize, x + r + tabSize, y);
        path.lineTo(x + size, y);
        
        path.lineTo(x + size, y + r - tabSize);
        path.curveTo(x + size + tabSize, y + r - tabSize/2, x + size + tabSize, y + r + tabSize/2, x + size, y + r + tabSize);
        path.lineTo(x + size, y + size);
        
        path.lineTo(x, y + size);
        path.lineTo(x, y);
        
        path.closePath();
        g2d.fill(path);
    }
    
    private void drawPuzzlePieceBorderOnly(Graphics2D g2d, int x, int y, int size) {
        int r = size / 2;
        int tabSize = size / 4;
        
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(x, y);
        
        path.lineTo(x + r - tabSize, y);
        path.curveTo(x + r - tabSize/2, y - tabSize, x + r + tabSize/2, y - tabSize, x + r + tabSize, y);
        path.lineTo(x + size, y);
        
        path.lineTo(x + size, y + r - tabSize);
        path.curveTo(x + size + tabSize, y + r - tabSize/2, x + size + tabSize, y + r + tabSize/2, x + size, y + r + tabSize);
        path.lineTo(x + size, y + size);
        
        path.lineTo(x, y + size);
        path.lineTo(x, y);
        
        path.closePath();
        g2d.draw(path);
    }
    
    private void drawPuzzlePieceHighlight(Graphics2D g2d, int x, int y, int size) {
        int r = size / 2;
        int tabSize = size / 4;
        
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(x + 3, y + 3);
        
        path.lineTo(x + r - tabSize, y + 3);
        path.curveTo(x + r - tabSize/2, y - tabSize + 3, x + r + tabSize/2, y - tabSize + 3, x + r + tabSize, y + 3);
        path.lineTo(x + size - 3, y + 3);
        
        path.lineTo(x + size - 3, y + r - tabSize);
        
        path.closePath();
        g2d.draw(path);
    }
    
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
