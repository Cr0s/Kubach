package kubach.packet;

/**
 * @author Cr0s
 */
public class PacketCaptchaImage {
    public boolean hasCaptcha;
    
    public byte[] imageData; // this is byte[]
    public String key;
    
    public PacketCaptchaImage() {
        
    }
    
    @Override
    public String toString() {
        return "Captcha (" + this.hasCaptcha + ") : " + this.key;
    }
}
