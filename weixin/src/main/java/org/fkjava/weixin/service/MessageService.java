package org.fkjava.weixin.service;

import org.fkjava.commons.domain.InMessage;
import org.fkjava.commons.domain.OUtMessage;

public interface MessageService {

	OUtMessage onMessage(InMessage msg);
}
