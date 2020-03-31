/*
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
package com.bonree.brfs.netty;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.ContainerUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * {@link io.netty.channel.ChannelInboundHandler} which servers as a bridge
 * between Netty and Jersey. Handles additional validation on the payload size
 * that is controlled by a JVM property {@code max.http.request.entitySizeMb}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class JerseyServerHandler extends ChannelInboundHandlerAdapter {

    private final URI baseUri;
    private final NettyInputStream nettyInputStream = new NettyInputStream();
    private final NettyHttpContainer container;
    private final ResourceConfig resourceConfig;

    /**
     * Constructor.
     *
     * @param baseUri   base {@link URI} of the container (includes context path, if any).
     * @param container Netty container implementation.
     */
    public JerseyServerHandler(URI baseUri, NettyHttpContainer container, ResourceConfig resourceConfig) {
        this.baseUri = baseUri;
        this.container = container;
        this.resourceConfig = resourceConfig;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            final HttpRequest req = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            }

            nettyInputStream.clear(); // clearing the content - possible leftover from previous request processing.
            final ContainerRequest requestContext = createContainerRequest(ctx, req);

            requestContext.setWriter(new NettyResponseWriter(ctx, req, container));

            long contentLength = req.headers().contains(HttpHeaderNames.CONTENT_LENGTH) ? HttpUtil.getContentLength(req)
                    : -1L;
            
            /**
             * Jackson JSON decoder tries to read a minimum of 2 bytes (4
             * for BOM). So, during an empty or 1-byte input, we'd want to
             * avoid reading the entity to safely handle this edge case by
             * eventually throwing a malformed JSON exception.
             */
            String contentType = req.headers().get(HttpHeaderNames.CONTENT_TYPE);
            boolean isJson = contentType != null ? contentType.toLowerCase().contains(MediaType.APPLICATION_JSON)
                    : false;
            //process entity streams only if there is an entity issued in the request (i.e., content-length >=0).
            //Otherwise, it's safe to discard during next processing
            if ((!isJson && contentLength != -1) || HttpUtil.isTransferEncodingChunked(req)
                    || (isJson && contentLength >= 2)) {
                requestContext.setEntityStream(nettyInputStream);
            }

            // copying headers from netty request to jersey container request context.
            for (String name : req.headers().names()) {
                requestContext.headers(name, req.headers().getAll(name));
            }

            // must be like this, since there is a blocking read from Jersey
            container.getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    container.getApplicationHandler().handle(requestContext);
                }
            });
        }

        if (msg instanceof HttpContent) {
          HttpContent httpContent = (HttpContent) msg;

          ByteBuf content = httpContent.content();
          if (content.isReadable()) {
              nettyInputStream.publish(content);
          }

          if (msg instanceof LastHttpContent) {
              nettyInputStream.complete(null);
          }
      }
    }

    /**
     * Create Jersey {@link ContainerRequest} based on Netty {@link HttpRequest}.
     *
     * @param ctx Netty channel context.
     * @param req Netty Http request.
     * @return created Jersey Container Request.
     */
    private ContainerRequest createContainerRequest(ChannelHandlerContext ctx, HttpRequest req) {

        String s = req.uri().startsWith("/") ? req.uri().substring(1) : req.uri();
        URI requestUri = URI.create(baseUri + ContainerUtils.encodeUnsafeCharacters(s));

        ContainerRequest requestContext = new ContainerRequest(
                baseUri, requestUri, req.method().name(), getSecurityContext(ctx),
                new PropertiesDelegate() {

                    private final Map<String, Object> properties = new HashMap<>();

                    @Override
                    public Object getProperty(String name) {
                        return properties.get(name);
                    }

                    @Override
                    public Collection<String> getPropertyNames() {
                        return properties.keySet();
                    }

                    @Override
                    public void setProperty(String name, Object object) {
                        properties.put(name, object);
                    }

                    @Override
                    public void removeProperty(String name) {
                        properties.remove(name);
                    }
                }, resourceConfig);

        return requestContext;
    }


    private NettySecurityContext getSecurityContext(ChannelHandlerContext ctx) {
        return new NettySecurityContext(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}