package se.hms.argos.afutest.infra.modbustcp;

import java.net.InetAddress;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Wrapper for managing an embedded ActiveMQ broker.
 *
 */
public class ModbustcpServer implements InitializingBean, DisposableBean
{

   private static final Logger logger = LoggerFactory.getLogger(ModbustcpServer.class);

   private final ModbustcpConfig config;

   public ModbusTCPListener listener = null;
   public SimpleProcessImage spi = null;

   // TODO: Fix how to control registers from other part of the code
   public SimpleRegister publicRegister = new SimpleRegister(252);

   public ModbustcpServer(ModbustcpConfig config) throws Exception
   {
      this.config = config;
   }

   @Override
   public void afterPropertiesSet()
   {
      start();
   }

   protected boolean start()
   {
      try
      {
         logger.info("ModbustcpServer starting");

         // TODO: Check if this is a good way starting the Modbus/TCP Server

         // 2. Prepare a process image
         spi = new SimpleProcessImage();
         spi.addRegister(new SimpleRegister(251));
         spi.addInputRegister(new SimpleInputRegister(45));

         // 3. Set the image on the coupler
         ModbusCoupler.getReference().setProcessImage(spi);
         ModbusCoupler.getReference().setMaster(false);
         ModbusCoupler.getReference().setUnitID(15);

         listener = new ModbusTCPListener(3);
         listener.setAddress(InetAddress.getByName("0.0.0.0"));
         listener.setPort(config.modbusTcpServerPort());
         listener.start();

         // Register more after start.
         // TODO: Test if this works
         // TODO: How can registers be controlled in a better way?
         spi.addRegister(publicRegister);
         spi.addRegister(new SimpleRegister(253));

         // TODO: Is it possible to know when a client read values?

         return true;
      }
      catch (Exception e)
      {
         logger.error("Modbus problem", e);
         return false;
      }
   }

   @Override
   public void destroy() throws Exception
   {
      listener.stop();
   }
}
