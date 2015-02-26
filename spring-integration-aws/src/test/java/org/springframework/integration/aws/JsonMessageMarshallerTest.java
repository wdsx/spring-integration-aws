package org.springframework.integration.aws;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.integration.aws.support.TestPojo;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@RunWith(JUnit4.class)
public class JsonMessageMarshallerTest {

	private JsonMessageMarshaller marshaller;

	@Before
	public void setup() {
		marshaller = new JsonMessageMarshaller();
	}

	@Test
	public void testSerialization() throws MessageMarshallerException {

		TestPojo pojo = new TestPojo();
		pojo.setName("John Doe");
		pojo.setEmail("user@example.com");

		String json = marshaller.serialize(MessageBuilder.withPayload(pojo)
				.build());

		TestPojo otherPojo = (TestPojo) marshaller.deserialize(json)
				.getPayload();

		assertEquals(pojo, otherPojo);
	}

	@Test
	public void testMessageHeaders() throws MessageMarshallerException {

		TestPojo pojo = new TestPojo();
		pojo.setName("John Doe");
		pojo.setEmail("user@example.com");
		long expiryTime = new Date().getTime();
		Long[] ary = new Long[] { 1L, 2L, 3L };
		Message<String> sent = MessageBuilder.withPayload("Hello, World")
				.setCorrelationId("ABC").setExpirationDate(expiryTime)
				.setPriority(2).setSequenceNumber(2).setSequenceSize(5)
				.setHeader("fubar", "FUBAR").setHeader("pojo", pojo)
				.setHeader("ary", ary).build();

		String original = marshaller.serialize(sent);

		Message<?> received = marshaller.deserialize(original);

		assertEquals(sent.getPayload(), received.getPayload());
		MessageHeaders sentHeaders = sent.getHeaders();
		MessageHeaders recvHeaders = received.getHeaders();
		assertEquals(sentHeaders.get(HeaderKeys.CORRELATION_ID), recvHeaders.get(HeaderKeys.CORRELATION_ID));
		assertEquals(new Long(expiryTime), recvHeaders.get(HeaderKeys.EXPIRATION_DATE));
		assertEquals(sentHeaders.get(HeaderKeys.PRIORITY), recvHeaders.get(HeaderKeys.PRIORITY));
		assertEquals(sentHeaders.get(HeaderKeys.SEQUENCE_NUMBER),
				recvHeaders.get(HeaderKeys.SEQUENCE_NUMBER));
		assertEquals(sentHeaders.get(HeaderKeys.SEQUENCE_SIZE),
				recvHeaders.get(HeaderKeys.SEQUENCE_SIZE));
		assertEquals(sentHeaders.get("fubar", String.class),
				recvHeaders.get("fubar", String.class));
		assertEquals(pojo, recvHeaders.get("pojo", TestPojo.class));
		assertTrue(Arrays.deepEquals(ary, (Object[]) recvHeaders.get("ary")));
	}

	@Test
	public void testPrimitivesPayload() throws MessageMarshallerException {

		Integer i = new Integer(1);
		String packet = marshaller.serialize(MessageBuilder.withPayload(i)
				.build());

		Message<?> recvd = marshaller.deserialize(packet);
		Integer j = (Integer) recvd.getPayload();

		assertEquals(i, j);
	}

	@Test
	public void testArrayofPrimitivesPayload()
			throws MessageMarshallerException {

		Integer[] aryIn = new Integer[] { 1, 2 };

		String packet = marshaller.serialize(MessageBuilder.withPayload(aryIn)
				.build());
		Message<?> otherPacket = marshaller.deserialize(packet);
		Integer[] aryOut = (Integer[]) otherPacket.getPayload();

		assertTrue(Arrays.deepEquals(aryIn, aryOut));
	}

	@Test
	public void testArrayOfPojo() throws MessageMarshallerException {

		TestPojo[] aryIn = new TestPojo[2];
		aryIn[0] = new TestPojo();
		aryIn[0].setName("John Doe");
		aryIn[0].setEmail("user@example.com");
		aryIn[1] = new TestPojo();
		aryIn[1].setName("Lionel Messi");
		aryIn[1].setEmail("messi@example.com");

		String packet = marshaller.serialize(MessageBuilder.withPayload(aryIn)
				.build());
		Message<?> otherPacket = marshaller.deserialize(packet);

		TestPojo[] aryOut = (TestPojo[]) otherPacket.getPayload();

		assertTrue(Arrays.deepEquals(aryIn, aryOut));

	}

	@Test
	public void testSimpleMessage() throws MessageMarshallerException {

		String simpleMessage = "Hello World";
		Message<?> packet = marshaller.deserialize(simpleMessage);

		assertEquals(simpleMessage, packet.getPayload());
	}
	
	private abstract class HeaderKeys {
		public static final String CORRELATION_ID = "correlationId";
		private static final String EXPIRATION_DATE = "expirationDate";
		private static final String PRIORITY = "priority";
		private static final String SEQUENCE_NUMBER = "sequenceNumber";
		private static final String SEQUENCE_SIZE = "sequenceSize";
	}

}
