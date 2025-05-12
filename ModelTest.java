import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class ModelTest {
    private Model model;
    
    @Before
    public void setUp() {
        model = new Model();
    }
    
    /**
     * 测试场景1：测试基本的游戏流程
     * - 验证初始状态
     * - 验证有效移动
     * - 验证获胜条件
     */
    @Test
    public void testBasicGameFlow() {
        // 验证初始状态
        assertEquals("sale", model.getStartWord());
        assertEquals("same", model.getTargetWord());
        assertTrue(model.getGameHistory().isEmpty());
        
        // 测试有效移动
        assertTrue(model.makeMove("sale"));  // 起始词
        assertTrue(model.makeMove("same"));  // 目标词
        
        // 验证游戏历史
        List<String> history = model.getGameHistory();
        assertEquals(2, history.size());
        assertEquals("sale", history.get(0));
        assertEquals("same", history.get(1));
        
        // 验证获胜状态
        assertTrue(model.hasWon());
    }
    
    /**
     * 测试场景2：测试无效移动
     * - 测试非字典单词
     * - 测试改变多个字母
     * - 测试错误消息开关
     */
    @Test
    public void testInvalidMoves() {
        // 测试非字典单词
        assertFalse(model.makeMove("xxxx"));
        
        // 测试改变多个字母
        assertTrue(model.makeMove("sale"));  // 有效的第一步
        assertFalse(model.makeMove("same")); // 不能直接跳到目标词
        
        // 测试错误消息开关
        model.setShowErrorMessage(false);
        assertFalse(model.makeMove("xxxx"));
        assertTrue(model.getGameHistory().size() == 1); // 历史记录应该只有一个词
        
        model.setShowErrorMessage(true);
        assertFalse(model.makeMove("xxxx"));
    }
    
    /**
     * 测试场景3：测试随机单词功能
     * - 测试随机单词开关
     * - 验证随机单词的有效性
     * - 测试游戏重置
     */
    @Test
    public void testRandomWordsAndReset() {
        // 记录初始单词
        String originalStart = model.getStartWord();
        String originalTarget = model.getTargetWord();
        
        // 启用随机单词
        model.setUseRandomWords(true);
        model.initializeGame();
        
        // 验证新单词与原单词不同
        String newStart = model.getStartWord();
        String newTarget = model.getTargetWord();
        assertFalse(originalStart.equals(newStart) && originalTarget.equals(newTarget));
        
        // 验证随机单词的有效性
        assertTrue(model.isValidWord(newStart));
        assertTrue(model.isValidWord(newTarget));
        assertNotEquals(newStart, newTarget);
        
        // 测试游戏重置
        assertTrue(model.makeMove(newStart));
        assertTrue(model.makeMove("word")); // 假设这是一个有效的中间词
        assertEquals(2, model.getGameHistory().size());
        
        model.initializeGame();
        assertTrue(model.getGameHistory().isEmpty());
    }
    
    /**
     * 测试场景4：测试路径显示功能
     */
    @Test
    public void testPathDisplay() {
        assertFalse(model.isShowPath());
        model.setShowPath(true);
        assertTrue(model.isShowPath());
        
        // 测试一个完整的游戏路径
        assertTrue(model.makeMove("sale"));
        assertTrue(model.makeMove("male"));
        assertTrue(model.makeMove("mame"));
        assertTrue(model.makeMove("same"));
        
        List<String> path = model.getGameHistory();
        assertEquals(4, path.size());
        assertEquals("same", path.get(path.size() - 1));
    }
} 