/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vxms.event.response;

import io.vertx.core.eventbus.Message;

/**
 * Created by Andy Moncsek on 12.01.16. his class allows easy access to event-bus message to get the
 * body and the reply address
 */
public class EventbusRequest {

  private final Message<Object> message;

  /**
   * init the EventbusBridgeRequest
   *
   * @param message the event-bus message
   */
  public EventbusRequest(Message<Object> message) {
    this.message = message;
  }

  /**
   * Returns the body of the message
   *
   * @param <T> the type of payload
   * @return the payloasd
   */
  public <T> T body() {
    return (T) message.body();
  }

  /**
   * Returns the reply-address to reply to the incoming message
   *
   * @return the reply address
   */
  public String replyAddress() {
    return message.replyAddress();
  }
}
